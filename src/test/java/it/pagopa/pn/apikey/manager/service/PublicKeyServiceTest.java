package it.pagopa.pn.apikey.manager.service;

import it.pagopa.pn.apikey.manager.converter.PublicKeyConverter;
import it.pagopa.pn.apikey.manager.entity.PublicKeyModel;
import it.pagopa.pn.apikey.manager.exception.PnForbiddenException;
import it.pagopa.pn.apikey.manager.generated.openapi.server.v1.dto.CxTypeAuthFleetDto;
import it.pagopa.pn.apikey.manager.generated.openapi.server.v1.dto.PublicKeyRowDto;
import it.pagopa.pn.apikey.manager.generated.openapi.server.v1.dto.PublicKeyStatusDto;
import it.pagopa.pn.apikey.manager.generated.openapi.server.v1.dto.PublicKeysResponseDto;
import it.pagopa.pn.apikey.manager.repository.PublicKeyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import software.amazon.awssdk.enhanced.dynamodb.model.Page;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
class PublicKeyServiceTest {

    private PublicKeyService publicKeyService;
    private PublicKeyRepository publicKeyRepository;

    @BeforeEach
    void setUp() {
        publicKeyRepository = mock(PublicKeyRepository.class);
        publicKeyService = new PublicKeyService(publicKeyRepository, new PublicKeyConverter());
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

        when(publicKeyRepository.getAllPaginated(any(), any(), any())).thenReturn(Mono.just(page));

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
                .expectError(PnForbiddenException.class)
                .verify();
    }


}