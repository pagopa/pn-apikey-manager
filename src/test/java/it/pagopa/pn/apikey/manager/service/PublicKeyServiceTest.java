package it.pagopa.pn.apikey.manager.service;

import it.pagopa.pn.apikey.manager.config.PnApikeyManagerConfig;
import it.pagopa.pn.apikey.manager.converter.PublicKeyConverter;
import it.pagopa.pn.apikey.manager.entity.PublicKeyModel;
import it.pagopa.pn.apikey.manager.exception.ApiKeyManagerException;
import it.pagopa.pn.apikey.manager.exception.ApiKeyManagerExceptionError;
import it.pagopa.pn.apikey.manager.generated.openapi.server.v1.dto.*;
import it.pagopa.pn.apikey.manager.middleware.queue.consumer.event.PublicKeyEvent;
import it.pagopa.pn.apikey.manager.repository.PublicKeyRepository;
import it.pagopa.pn.apikey.manager.validator.PublicKeyValidator;
import it.pagopa.pn.commons.log.PnAuditLogBuilder;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import software.amazon.awssdk.enhanced.dynamodb.model.Page;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import static it.pagopa.pn.apikey.manager.exception.ApiKeyManagerExceptionError.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
class PublicKeyServiceTest {

    private PublicKeyService publicKeyService;
    private PublicKeyRepository publicKeyRepository;
    private PublicKeyValidator validator;
    private final PnAuditLogBuilder auditLogBuilder = new PnAuditLogBuilder();
    private LambdaService lambdaService;
    @MockBean
    private PnApikeyManagerConfig pnApikeyManagerConfig;

    @BeforeEach
    void setUp() {
        publicKeyRepository = Mockito.mock(PublicKeyRepository.class);
        lambdaService = Mockito.mock(LambdaService.class);
        pnApikeyManagerConfig = mock(PnApikeyManagerConfig.class);
        when(pnApikeyManagerConfig.getLambdaName()).thenReturn("lambdaName");
        when(pnApikeyManagerConfig.getEnableJwksCreation()).thenReturn(true);
        validator = new PublicKeyValidator(publicKeyRepository);
        publicKeyService = new PublicKeyService(publicKeyRepository, lambdaService, auditLogBuilder, validator, pnApikeyManagerConfig, new PublicKeyConverter());
    }

    @Test
    void deletePublicKey_Success() {
        PublicKeyModel publicKeyModel = new PublicKeyModel();
        publicKeyModel.setKid("kid");
        publicKeyModel.setStatusHistory(List.of());
        publicKeyModel.setStatus("BLOCKED");

        when(publicKeyRepository.findByKidAndCxId(any(), any())).thenReturn(Mono.just(publicKeyModel));
        when(publicKeyRepository.save(any())).thenReturn(Mono.just(publicKeyModel));

        Mono<String> result = publicKeyService.deletePublicKey("uid", CxTypeAuthFleetDto.PG, "cxId", "kid", null, "ADMIN");

        StepVerifier.create(result)
                .expectNext("Public key deleted")
                .verifyComplete();
    }

    @Test
    void deletePublicKey_Conflict() {
        PublicKeyModel publicKeyModel = new PublicKeyModel();
        publicKeyModel.setKid("kid");
        publicKeyModel.setStatusHistory(List.of());
        publicKeyModel.setStatus("DELETED");

        when(publicKeyRepository.findByKidAndCxId(any(), any())).thenReturn(Mono.just(publicKeyModel));
        when(publicKeyRepository.save(any())).thenReturn(Mono.just(publicKeyModel));

        Mono<String> result = publicKeyService.deletePublicKey("uid", CxTypeAuthFleetDto.PG, "cxId", "kid", null, "ADMIN");

        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof ApiKeyManagerException && ((ApiKeyManagerException) throwable).getStatus() == HttpStatus.CONFLICT)
                .verify();
    }

    @Test
    void deletePublicKey_CxTypeNotAllowed() {
        Mono<String> result = publicKeyService.deletePublicKey("uid", CxTypeAuthFleetDto.PA, "cxId", "kid", null, "ADMIN");

        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof ApiKeyManagerException && ((ApiKeyManagerException) throwable).getStatus() == HttpStatus.FORBIDDEN)
                .verify();
    }

    @ParameterizedTest
    @CsvSource({
            "BLOCKED, ENABLE",
            "ACTIVE, BLOCK"
    })
    void changeStatus_updatesStatusSuccessfully(String apiKeyStatus, String statusToUpdate) {
        PublicKeyModel publicKeyModel = mockPublicKeyModel(apiKeyStatus);


        when(publicKeyRepository.findByKidAndCxId(any(), any())).thenReturn(Mono.just(publicKeyModel));
        when(publicKeyRepository.save(any())).thenReturn(Mono.just(publicKeyModel));
        when(publicKeyRepository.findByCxIdAndStatus(any(), any())).thenReturn(Flux.empty());

        StepVerifier.create(publicKeyService.changeStatus("kid", statusToUpdate, "uid", CxTypeAuthFleetDto.PG, "cxId", List.of(), "ADMIN"))
                .verifyComplete();
    }

    @NotNull
    private static PublicKeyModel mockPublicKeyModel(String apiKeyStatus) {
        PublicKeyModel publicKeyModel = new PublicKeyModel();
        publicKeyModel.setKid("kid");
        publicKeyModel.setName("Test Key");
        publicKeyModel.setCorrelationId("correlationId");
        publicKeyModel.setPublicKey("publicKeyData");
        publicKeyModel.setStatus(apiKeyStatus);
        publicKeyModel.setExpireAt(Instant.now().plus(1, ChronoUnit.DAYS));
        publicKeyModel.setIssuer("issuer");
        publicKeyModel.setCxId("cxId");
        publicKeyModel.setStatusHistory(new ArrayList<>());
        return publicKeyModel;
    }

    @Test
    void changeStatus_withInvalidRole_throwsForbiddenException() {
        StepVerifier.create(publicKeyService.changeStatus("kid", "ENABLE", "uid", CxTypeAuthFleetDto.PG, "cxId", List.of(), "USER"))
                .expectErrorMatches(throwable -> throwable instanceof ApiKeyManagerException && Objects.requireNonNull(throwable.getMessage()).contains(ApiKeyManagerExceptionError.ACCESS_DENIED))
                .verify();
    }

    @Test
    void changeStatus_withInvalidStatusTransition_throwsApiKeyManagerException() {
        PublicKeyModel publicKeyModel = new PublicKeyModel();
        publicKeyModel.setStatus("ACTIVE");

        when(publicKeyRepository.findByKidAndCxId(anyString(), anyString())).thenReturn(Mono.just(publicKeyModel));
        when(publicKeyRepository.findByCxIdAndStatus(any(), any())).thenReturn(Flux.empty());

        StepVerifier.create(publicKeyService.changeStatus("kid", "INACTIVE", "uid", CxTypeAuthFleetDto.PG, "cxId", List.of(), "ADMIN"))
                .expectErrorMatches(throwable -> throwable instanceof ApiKeyManagerException && throwable.getMessage().contains(ApiKeyManagerExceptionError.PUBLIC_KEY_INVALID_STATE_TRANSITION))
                .verify();
    }

    @Test
    void createPublicKey_withValidData_returnsPublicKeyResponseDto() {
        PublicKeyRequestDto requestDto = new PublicKeyRequestDto();
        requestDto.setName("Test Key");
        requestDto.setPublicKey("publicKeyData");

        PublicKeyModel publicKeyModel = new PublicKeyModel();
        publicKeyModel.setKid("kid");
        publicKeyModel.setName("Test Key");
        publicKeyModel.setCorrelationId("correlationId");
        publicKeyModel.setPublicKey("publicKeyData");
        publicKeyModel.setStatus("ACTIVE");
        publicKeyModel.setExpireAt(Instant.now().plus(1, ChronoUnit.DAYS));
        publicKeyModel.setIssuer("issuer");
        publicKeyModel.setCxId("cxId");

        PublicKeyModel publicKeyModelCopy = new PublicKeyModel();
        publicKeyModelCopy.setKid("kid_COPY");
        publicKeyModelCopy.setName("Test Key");
        publicKeyModelCopy.setCorrelationId("correlationId");
        publicKeyModelCopy.setPublicKey("publicKeyData");
        publicKeyModelCopy.setStatus("ACTIVE");
        publicKeyModelCopy.setExpireAt(Instant.now().plus(1, ChronoUnit.DAYS));
        publicKeyModelCopy.setIssuer("issuer");
        publicKeyModelCopy.setTtl(publicKeyModelCopy.getExpireAt().getEpochSecond());
        publicKeyModelCopy.setCxId("cxId");


        when(publicKeyRepository.findByCxIdAndStatus(anyString(), eq("ACTIVE"))).thenReturn(Flux.empty());
        when(publicKeyRepository.save(any())).thenReturn(Mono.just(publicKeyModel));
        when(publicKeyRepository.save(any())).thenReturn(Mono.just(publicKeyModelCopy));

        StepVerifier.create(publicKeyService.createPublicKey("uid", CxTypeAuthFleetDto.PG, "cxId", Mono.just(requestDto), List.of(), "ADMIN"))
                .expectNextMatches(response -> response.getKid() != null && response.getIssuer() != null)
                .verifyComplete();
    }

    @Test
    void createPublicKey_withExistingActiveKey_throwsApiKeyManagerException() {
        PublicKeyModel publicKeyModel = new PublicKeyModel();
        publicKeyModel.setKid("kid");
        publicKeyModel.setExpireAt(Instant.now().plus(1, ChronoUnit.DAYS));
        publicKeyModel.setIssuer("issuer");
        when(publicKeyRepository.findByCxIdAndStatus(anyString(), eq("ACTIVE"))).thenReturn(Flux.just(publicKeyModel));
        PublicKeyRequestDto dto = new PublicKeyRequestDto();
        dto.setName("Test Key");
        dto.setPublicKey("publicKey");

        StepVerifier.create(publicKeyService.createPublicKey("uid", CxTypeAuthFleetDto.PG, "cxId", Mono.just(dto), List.of(), "ADMIN"))
                .expectErrorMatches(throwable -> throwable instanceof ApiKeyManagerException && throwable.getMessage().contains(ApiKeyManagerExceptionError.PUBLIC_KEY_ALREADY_EXISTS_ACTIVE))
                .verify();
    }

    @Test
    void createPublicKey_withInvalidRole_throwsApiKeyManagerException() {
        StepVerifier.create(publicKeyService.createPublicKey("uid", CxTypeAuthFleetDto.PG, "cxId", Mono.just(new PublicKeyRequestDto()), List.of(), "invalidRole"))
                .expectErrorMatches(throwable -> throwable instanceof ApiKeyManagerException && throwable.getMessage().contains(ApiKeyManagerExceptionError.ACCESS_DENIED))
                .verify();
    }

    @Test
    void handlePublicKeyEventSuccessfully() {
        PublicKeyModel publicKeyModel = new PublicKeyModel();
        publicKeyModel.setExpireAt(Instant.now().minusSeconds(60));
        publicKeyModel.setKid("kid");
        publicKeyModel.setCxId("cxId");
        publicKeyModel.setStatus("DELETE");

        MessageHeaders messageHeaders = new MessageHeaders(null);
        PublicKeyEvent.Payload payload = PublicKeyEvent.Payload.builder().kid("kid").cxId("cxId").action("DELETE").build();
        Message<PublicKeyEvent.Payload> message = MessageBuilder.createMessage(payload, messageHeaders);

        when(publicKeyRepository.findByKidAndCxId(any(), any())).thenReturn(Mono.just(publicKeyModel));
        when(publicKeyRepository.save(any())).thenReturn(Mono.just(publicKeyModel));

        Mono<PublicKeyModel> result = publicKeyService.handlePublicKeyTtlEvent(message);

        StepVerifier.create(result)
                .expectNext(publicKeyModel)
                .verifyComplete();
    }

    @Test
    void handlePublicKeyEventKeyNotFound() {
        MessageHeaders messageHeaders = new MessageHeaders(null);
        PublicKeyEvent.Payload payload = PublicKeyEvent.Payload.builder().kid("kid").cxId("cxId").action("DELETE").build();
        Message<PublicKeyEvent.Payload> message = MessageBuilder.createMessage(payload, messageHeaders);

        when(publicKeyRepository.findByKidAndCxId(any(), any())).thenReturn(Mono.error(new RuntimeException("Key not found")));

        Mono<PublicKeyModel> result = publicKeyService.handlePublicKeyTtlEvent(message);

        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof RuntimeException &&
                        throwable.getMessage().equals("Key not found"))
                .verify();
    }

    @Test
    void handlePublicKeyEventKeyNotExpired() {
        PublicKeyModel publicKeyModel = new PublicKeyModel();
        publicKeyModel.setExpireAt(Instant.now().plusSeconds(60));
        publicKeyModel.setKid("kid");
        publicKeyModel.setCxId("cxId");
        publicKeyModel.setStatus("PENDING");

        MessageHeaders messageHeaders = new MessageHeaders(null);
        PublicKeyEvent.Payload payload = PublicKeyEvent.Payload.builder().kid("kid").cxId("cxId").action("DELETE").build();
        Message<PublicKeyEvent.Payload> message = MessageBuilder.createMessage(payload, messageHeaders);

        when(publicKeyRepository.findByKidAndCxId(any(), any())).thenReturn(Mono.just(publicKeyModel));
        when(publicKeyRepository.save(any())).thenReturn(Mono.just(publicKeyModel));

        Mono<PublicKeyModel> result = publicKeyService.handlePublicKeyTtlEvent(message);

        StepVerifier.create(result)
                .verifyComplete();
    }

    @Test
    void handlePublicKeyEventKeyAlreadyDeleted() {
        PublicKeyModel publicKeyModel = new PublicKeyModel();
        publicKeyModel.setExpireAt(Instant.now().minusSeconds(60));
        publicKeyModel.setKid("kid");
        publicKeyModel.setCxId("cxId");
        publicKeyModel.setStatus("DELETED");

        MessageHeaders messageHeaders = new MessageHeaders(null);
        PublicKeyEvent.Payload payload = PublicKeyEvent.Payload.builder().kid("kid").cxId("cxId").action("DELETE").build();
        Message<PublicKeyEvent.Payload> message = MessageBuilder.createMessage(payload, messageHeaders);

        when(publicKeyRepository.findByKidAndCxId(any(), any())).thenReturn(Mono.just(publicKeyModel));

        Mono<PublicKeyModel> result = publicKeyService.handlePublicKeyTtlEvent(message);

        StepVerifier.create(result)
                .verifyComplete();

        verify(publicKeyRepository, never()).updateItemStatus(any(), anyList());
    }

    @Test
    void handlePublicKeyEventWithoutKidAndCxId() {
        MessageHeaders messageHeaders = new MessageHeaders(null);
        PublicKeyEvent.Payload payload = PublicKeyEvent.Payload.builder().kid("").cxId("").action("DELETE").build();
        Message<PublicKeyEvent.Payload> message = MessageBuilder.createMessage(payload, messageHeaders);

        Mono<PublicKeyModel> result = publicKeyService.handlePublicKeyTtlEvent(message);

        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof RuntimeException &&
                        throwable.getMessage().equals(TTL_PAYLOAD_INVALID_KID_CXID))
                .verify();
    }

    @Test
    void handlePublicKeyEventInvalidAction() {
        MessageHeaders messageHeaders = new MessageHeaders(null);
        PublicKeyEvent.Payload payload = PublicKeyEvent.Payload.builder().kid("kid").cxId("cxId").action("RESUME").build();
        Message<PublicKeyEvent.Payload> message = MessageBuilder.createMessage(payload, messageHeaders);

        Mono<PublicKeyModel> result = publicKeyService.handlePublicKeyTtlEvent(message);

        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof RuntimeException &&
                        throwable.getMessage().equals(TTL_PAYLOAD_INVALID_ACTION))
                .verify();
    }

    @Test
    void rotatePublicKey_withValidData_returnsPublicKeyResponseDto() {
        PublicKeyRequestDto requestDto = new PublicKeyRequestDto();
        requestDto.setName("Test Key");
        requestDto.setPublicKey("newPublicKey");

        PublicKeyModel activePublicKey = new PublicKeyModel();
        activePublicKey.setKid("kid");
        activePublicKey.setName("Test Key");
        activePublicKey.setPublicKey("publicKeyData");
        activePublicKey.setStatus("ACTIVE");
        activePublicKey.setIssuer("cxId");

        PublicKeyModel rotatedPublicKey = new PublicKeyModel(activePublicKey);
        rotatedPublicKey.setStatus("ROTATED");

        PublicKeyModel newActivePublicKey = new PublicKeyModel();
        newActivePublicKey.setKid("new_kid");
        newActivePublicKey.setName("Test Key");
        newActivePublicKey.setCorrelationId("kid");
        newActivePublicKey.setPublicKey("newPublicKey");
        newActivePublicKey.setStatus("ACTIVE");
        newActivePublicKey.setIssuer("cxId");

        PublicKeyModel newActivePublicKeyCopy = new PublicKeyModel();
        newActivePublicKeyCopy.setKid("new_kid_COPY");


        when(publicKeyRepository.findByCxIdAndStatus(anyString(), eq("ROTATED"))).thenReturn(Flux.empty());
        when(publicKeyRepository.findByKidAndCxId(any(), any())).thenReturn(Mono.just(activePublicKey));

        // Il flusso di rotazione prevede 3 interazioni con il metodo di save.
        // Mock primo save = Update dello stato della chiave fornita in input e trovata su DB da ACTIVE a ROTATED
        when(publicKeyRepository.save(argThat(model -> model != null && "kid".equals(model.getKid()))))
                .thenReturn(Mono.just(rotatedPublicKey));
        // Mock secondo save = Creazione di una nuova chiave ACTIVE (non avendo il KID che è generato randomicamente posso solo controllare che non contenga "COPY" nel kid)
        when(publicKeyRepository.save(argThat(model -> model != null && !"COPY".contains(model.getKid()))))
                .thenReturn(Mono.just(newActivePublicKey));
        // Mock terzo save = Creazione della copia della nuova chiave ACTIVE
        when(publicKeyRepository.save(argThat(model -> model != null && "new_kid_COPY".equals(model.getKid()))))
                .thenReturn(Mono.just(newActivePublicKeyCopy));

        StepVerifier.create(publicKeyService.rotatePublicKey(Mono.just(requestDto), "uid", CxTypeAuthFleetDto.PG, "cxId", "kid", List.of(), "ADMIN"))
                // Verifico che in risposta arrivino i dati della nuova chiave creata e non della copia
                .expectNextMatches(response -> response.getKid().equalsIgnoreCase("new_kid") && response.getIssuer().equalsIgnoreCase("cxId"))
                .verifyComplete();
    }

    @Test
    void rotatePublicKey_withExistingRotatedKey_throwsApiKeyManagerException() {
        PublicKeyModel publicKeyModel = new PublicKeyModel();
        publicKeyModel.setKid("kid");
        publicKeyModel.setExpireAt(Instant.now().plus(1, ChronoUnit.DAYS));
        publicKeyModel.setIssuer("issuer");
        publicKeyModel.setStatus("ROTATED");
        when(publicKeyRepository.findByCxIdAndStatus(any(), any())).thenReturn(Flux.just(publicKeyModel));

        PublicKeyRequestDto dto = new PublicKeyRequestDto();
        dto.setName("Test Key");
        dto.setPublicKey("publicKey");

        StepVerifier.create(publicKeyService.rotatePublicKey(Mono.just(dto), "uid", CxTypeAuthFleetDto.PG, "cxId", "kid", List.of(), "ADMIN"))
                .expectErrorMatches(throwable -> throwable instanceof ApiKeyManagerException && throwable.getMessage().contains(String.format(PUBLIC_KEY_ALREADY_EXISTS, PublicKeyStatusDto.ROTATED.getValue())))
                .verify();
    }

    @Test
    void rotatePublicKey_withInvalidRole_throwsApiKeyManagerException() {
        PublicKeyRequestDto dto = new PublicKeyRequestDto();
        dto.setName("Test Key");
        dto.setPublicKey("publicKey");
        StepVerifier.create(publicKeyService.rotatePublicKey(Mono.just(dto), "uid", CxTypeAuthFleetDto.PG, "cxId", "kid", null, "USER"))
                .expectErrorMatches(throwable -> throwable instanceof ApiKeyManagerException && (((ApiKeyManagerException) throwable).getStatus() == HttpStatus.FORBIDDEN))
                .verify();
    }

    @Test
    void getPublicKeys_whenValidInput_shouldReturnPublicKeys() {
        CxTypeAuthFleetDto cxType = CxTypeAuthFleetDto.PG;
        String xPagopaPnCxId = "cxId";
        List<String> xPagopaPnCxGroups = List.of();
        String xPagopaPnCxRole = "admin";
        Integer limit = 10;
        String lastKey = "lastKey";
        String createdAt = "2024-08-25T10:15:30.00Z";
        Boolean showPublicKey = true;

        List<PublicKeyModel> publicKeyModels = new ArrayList<>();
        PublicKeyModel publicKeyModel = new PublicKeyModel();
        publicKeyModel.setKid("kid");
        publicKeyModel.setPublicKey("publicKey");
        publicKeyModel.setCreatedAt(Instant.parse(createdAt));
        publicKeyModel.setCxId("cxId");
        publicKeyModel.setName("name");
        publicKeyModel.setStatus("ACTIVE");
        publicKeyModel.setStatusHistory(new ArrayList<>());
        publicKeyModels.add(publicKeyModel);
        Page<PublicKeyModel> page = Page.create(publicKeyModels);

        PublicKeysResponseDto responseDto = new PublicKeysResponseDto();
        PublicKeyRowDto publicKeyRowDto = new PublicKeyRowDto();
        publicKeyRowDto.setValue("publicKey");
        publicKeyRowDto.setStatus(PublicKeyStatusDto.ACTIVE);
        publicKeyRowDto.setKid("kid");
        publicKeyRowDto.setName("name");
        publicKeyRowDto.setStatusHistory(null);
        publicKeyRowDto.setCreatedAt(Date.from(Instant.parse(createdAt)));
        responseDto.setItems(List.of(publicKeyRowDto));
        responseDto.setLastKey(null);
        responseDto.setCreatedAt(null);
        responseDto.setTotal(1);

        when(publicKeyRepository.getAllWithFilterPaginated(any(), any(), any())).thenReturn(Mono.just(page));
        when(publicKeyRepository.countWithFilters(any())).thenReturn(Mono.just(1));

        Mono<PublicKeysResponseDto> result = publicKeyService.getPublicKeys(cxType, xPagopaPnCxId, xPagopaPnCxGroups, xPagopaPnCxRole, limit, lastKey, createdAt, showPublicKey);

        StepVerifier.create(result)
                .expectNext(responseDto)
                .verifyComplete();
    }

    @Test
    void getPublicKeys_whenInvalidRole_shouldReturnError() {
        CxTypeAuthFleetDto cxType = CxTypeAuthFleetDto.PG;
        String xPagopaPnCxId = "cxId";
        List<String> xPagopaPnCxGroups = List.of();
        String xPagopaPnCxRole = "operator";
        Integer limit = 10;
        String lastKey = "lastKey";
        String createdAt = "createdAt";
        Boolean showPublicKey = true;

        Mono<PublicKeysResponseDto> result = publicKeyService.getPublicKeys(cxType, xPagopaPnCxId, xPagopaPnCxGroups, xPagopaPnCxRole, limit, lastKey, createdAt, showPublicKey);

        StepVerifier.create(result)
                .expectError(ApiKeyManagerException.class)
                .verify();
    }

    @Test
    void getIssuer_successActive() {
        List<PublicKeyModel> publicKeyModels = new ArrayList<>();
        PublicKeyModel publicKeyModel = new PublicKeyModel();
        publicKeyModel.setKid("kid");
        publicKeyModel.setPublicKey("publicKey");
        publicKeyModel.setCreatedAt(Instant.now());
        publicKeyModel.setCxId("cxId");
        publicKeyModel.setName("name");
        publicKeyModel.setStatus("ACTIVE");
        publicKeyModel.setIssuer("issuer");
        publicKeyModel.setStatusHistory(new ArrayList<>());
        publicKeyModels.add(publicKeyModel);
        Page<PublicKeyModel> page = Page.create(publicKeyModels);

        when(publicKeyRepository.findByCxIdAndWithoutTtl(any())).thenReturn(Mono.just(page));
        Mono<PublicKeysIssuerResponseDto> result = publicKeyService.getIssuer("cxId", CxTypeAuthFleetDto.PG);

        StepVerifier.create(result)
                .expectNextMatches(dto -> dto.getIsPresent() && dto.getIssuerStatus() == PublicKeysIssuerResponseDto.IssuerStatusEnum.ACTIVE)
                .verifyComplete();
    }

    @Test
    void getIssuer_successInactive() {
        List<PublicKeyModel> publicKeyModels = new ArrayList<>();
        PublicKeyModel publicKeyModel = new PublicKeyModel();
        publicKeyModel.setKid("kid");
        publicKeyModel.setPublicKey("publicKey");
        publicKeyModel.setCreatedAt(Instant.now());
        publicKeyModel.setCxId("cxId");
        publicKeyModel.setName("name");
        publicKeyModel.setStatus("BLOCKED");
        publicKeyModel.setIssuer("issuer");
        publicKeyModel.setStatusHistory(new ArrayList<>());
        publicKeyModels.add(publicKeyModel);
        Page<PublicKeyModel> page = Page.create(publicKeyModels);

        when(publicKeyRepository.findByCxIdAndWithoutTtl(any())).thenReturn(Mono.just(page));
        Mono<PublicKeysIssuerResponseDto> result = publicKeyService.getIssuer("cxId", CxTypeAuthFleetDto.PG);

        StepVerifier.create(result)
                .expectNextMatches(dto -> dto.getIsPresent() && dto.getIssuerStatus() == PublicKeysIssuerResponseDto.IssuerStatusEnum.INACTIVE)
                .verifyComplete();
    }

    @Test
    void getIssuer_empty() {
        List<PublicKeyModel> publicKeyModels = new ArrayList<>();
        Page<PublicKeyModel> page = Page.create(publicKeyModels);

        when(publicKeyRepository.findByCxIdAndWithoutTtl(any())).thenReturn(Mono.just(page));
        Mono<PublicKeysIssuerResponseDto> result = publicKeyService.getIssuer("cxId", CxTypeAuthFleetDto.PG);

        StepVerifier.create(result)
                .expectNextMatches(dto -> !dto.getIsPresent() && dto.getIssuerStatus() == null)
                .verifyComplete();
    }

    @Test
    void getIssuer_Forbidden() {
        Mono<PublicKeysIssuerResponseDto> result = publicKeyService.getIssuer("cxId", CxTypeAuthFleetDto.PF);

        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof ApiKeyManagerException && ((ApiKeyManagerException) throwable).getStatus() == HttpStatus.FORBIDDEN)
                .verify();
    }

    @Test
    void handlePublicKeyEvent_shouldInvokeLambda_whenActiveOrRotatedPublicKeyExists() {
        // Arrange
        String cxId = "testCxId";

        PublicKeyModel mockPublicKeyModel = new PublicKeyModel();
        mockPublicKeyModel.setStatus("ACTIVE");
        mockPublicKeyModel.setPublicKey("testPublicKey");
        mockPublicKeyModel.setKid("testKid");

        when(publicKeyRepository.findByCxIdAndStatus(cxId, null)).thenReturn(Flux.just(mockPublicKeyModel));
        when(lambdaService.invokeLambda(any(), any(), any()))
                .thenReturn(Mono.empty());

        // Act
        Mono<Void> result = publicKeyService.handlePublicKeyEvent(cxId);

        // Assert
        StepVerifier.create(result)
                .verifyComplete();

        verify(publicKeyRepository, times(1)).findByCxIdAndStatus(cxId, null);
        verify(lambdaService, times(1)).invokeLambda(any(), any(), any());
    }

    @Test
    void handlePublicKeyEvent_shouldInvokeLambda_whenActiveAndRotatedPublicKeyExists() {
        // Arrange
        String cxId = "testCxId";
        when(pnApikeyManagerConfig.getEnableJwksCreation()).thenReturn(true);

        PublicKeyModel mockPublicKeyModelActive = new PublicKeyModel();
        mockPublicKeyModelActive.setStatus("ACTIVE");
        mockPublicKeyModelActive.setPublicKey("testPublicKey");
        mockPublicKeyModelActive.setKid("testKid");

        PublicKeyModel mockPublicKeyModelRotated = new PublicKeyModel();
        mockPublicKeyModelRotated.setStatus("ROTATED");
        mockPublicKeyModelRotated.setPublicKey("testPublicKey");
        mockPublicKeyModelRotated.setKid("testKid");

        when(publicKeyRepository.findByCxIdAndStatus(cxId, null)).thenReturn(Flux.just(mockPublicKeyModelActive, mockPublicKeyModelRotated));
        when(lambdaService.invokeLambda(any(), any(), any()))
                .thenReturn(Mono.empty());

        // Act
        Mono<Void> result = publicKeyService.handlePublicKeyEvent(cxId);

        // Assert
        StepVerifier.create(result)
                .verifyComplete();

        verify(publicKeyRepository, times(1)).findByCxIdAndStatus(cxId, null);
        verify(lambdaService, times(1)).invokeLambda(any(), any(), any());
    }


    @Test
    void handlePublicKeyEvent_shouldHandleError_whenLambdaInvocationFails() {
        // Arrange
        String cxId = "testCxId";
        when(pnApikeyManagerConfig.getEnableJwksCreation()).thenReturn(true);

        PublicKeyModel mockPublicKeyModel = new PublicKeyModel();
        mockPublicKeyModel.setStatus("ACTIVE");
        mockPublicKeyModel.setPublicKey("testPublicKey");
        mockPublicKeyModel.setKid("testKid");

        when(publicKeyRepository.findByCxIdAndStatus(cxId, null)).thenReturn(Flux.just(mockPublicKeyModel));
        when(lambdaService.invokeLambda(any(), any(), any()))
                .thenReturn(Mono.error(new RuntimeException("Lambda invocation failed")));

        // Act
        Mono<Void> result = publicKeyService.handlePublicKeyEvent(cxId);

        // Assert
        StepVerifier.create(result)
                .expectError(RuntimeException.class)
                .verify();

        verify(publicKeyRepository, times(1)).findByCxIdAndStatus(cxId, null);
        verify(lambdaService, times(1)).invokeLambda(any(),any(), any());
    }

    @Test
    void handlePublicKeyEvent_shouldSkipExecution_ifFeatureFlagIsDisabled() {
        // Arrange
        String cxId = "testCxId";
        when(pnApikeyManagerConfig.getEnableJwksCreation()).thenReturn(false);

        // Act
        Mono<Void> result = publicKeyService.handlePublicKeyEvent(cxId);

        // Assert
        StepVerifier.create(result)
                .expectComplete()
                .verify();

        verify(publicKeyRepository, times(0)).findByCxIdAndStatus(cxId, null);
        verify(lambdaService, times(0)).invokeLambda(any(),any(), any());
    }
}