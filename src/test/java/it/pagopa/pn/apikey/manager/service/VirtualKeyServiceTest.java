package it.pagopa.pn.apikey.manager.service;

import it.pagopa.pn.apikey.manager.apikey.manager.generated.openapi.msclient.pnuserattributes.v1.dto.ConsentDto;
import it.pagopa.pn.apikey.manager.apikey.manager.generated.openapi.msclient.pnuserattributes.v1.dto.ConsentTypeDto;
import it.pagopa.pn.apikey.manager.client.PnUserAttributesClient;
import it.pagopa.pn.apikey.manager.config.PnApikeyManagerConfig;
import it.pagopa.pn.apikey.manager.entity.ApiKeyModel;
import it.pagopa.pn.apikey.manager.exception.ApiKeyManagerException;
import it.pagopa.pn.apikey.manager.generated.openapi.server.v1.dto.CxTypeAuthFleetDto;
import it.pagopa.pn.apikey.manager.generated.openapi.server.v1.dto.RequestNewVirtualKeyDto;
import it.pagopa.pn.apikey.manager.generated.openapi.server.v1.dto.ResponseNewVirtualKeyDto;
import it.pagopa.pn.apikey.manager.generated.openapi.server.v1.dto.VirtualKeyStatusDto;
import it.pagopa.pn.apikey.manager.repository.ApiKeyRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedAsyncClient;
import software.amazon.awssdk.enhanced.dynamodb.model.Page;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest
@ExtendWith(SpringExtension.class)
class VirtualKeyServiceTest {

    @Autowired
    private VirtualKeyService virtualKeyService;

    @MockBean
    private DynamoDbEnhancedAsyncClient dynamoDbEnhancedAsyncClient;

    @MockBean
    private PnUserAttributesClient pnUserAttributesClient;

    @MockBean
    private ApiKeyRepository apiKeyRepository;

    @MockBean
    private PnApikeyManagerConfig pnApikeyManagerConfig;

    @Test
    void createVirtualKey_Success() {
        RequestNewVirtualKeyDto requestDto = new RequestNewVirtualKeyDto();
        requestDto.setName("Test Key");
        ApiKeyModel apiKeyModel = new ApiKeyModel();
        apiKeyModel.setId("id");
        apiKeyModel.setVirtualKey("virtualKey");

        when(pnUserAttributesClient.getConsentByType(any(), any(), any(), any())).thenReturn(Mono.just(new ConsentDto().accepted(true)));
        when(apiKeyRepository.findByUidAndCxIdAndStatusAndScope(any(), any(), any(), any())).thenReturn(Mono.empty());
        when(apiKeyRepository.save(any())).thenReturn(Mono.just(apiKeyModel));

        StepVerifier.create(virtualKeyService.createVirtualKey("uid", CxTypeAuthFleetDto.PG, "cxId", Mono.just(requestDto)))
                .expectNextMatches(response -> response.getId().equals("id") && response.getVirtualKey().equals("virtualKey"))
                .verifyComplete();
    }

    @Test
    void createVirtualKey_CxTypeNotAllowed() {
        RequestNewVirtualKeyDto requestDto = new RequestNewVirtualKeyDto();

        StepVerifier.create(virtualKeyService.createVirtualKey("uid", CxTypeAuthFleetDto.PA, "cxId", Mono.just(requestDto)))
                .verifyErrorMatches(throwable -> throwable instanceof ApiKeyManagerException && throwable.getMessage().contains("CxTypeAuthFleet PA not allowed"));
    }

    @Test
    void createVirtualKey_TosConsentNotFound() {
        RequestNewVirtualKeyDto requestDto = new RequestNewVirtualKeyDto();

        when(apiKeyRepository.findByUidAndCxIdAndStatusAndScope(any(), any(), any(), any())).thenReturn(Mono.empty());
        when(pnUserAttributesClient.getConsentByType(any(), any(), any(), any())).thenReturn(Mono.just(new ConsentDto().accepted(false)));


        StepVerifier.create(virtualKeyService.createVirtualKey("uid", CxTypeAuthFleetDto.PG, "cxId", Mono.just(requestDto)))
                .verifyErrorMatches(throwable -> throwable instanceof ApiKeyManagerException && throwable.getMessage().contains("TOS consent not found"));
    }

    @Test
    void createVirtualKey_InternalError() {
        RequestNewVirtualKeyDto requestDto = new RequestNewVirtualKeyDto();

        when(pnUserAttributesClient.getConsentByType(any(), any(), any(), any())).thenReturn(Mono.just(new ConsentDto().accepted(true)));
        when(apiKeyRepository.findByUidAndCxIdAndStatusAndScope(any(), any(), any(), any())).thenReturn(Mono.empty());
        when(apiKeyRepository.save(any())).thenReturn(Mono.error(new RuntimeException("Internal error")));

        StepVerifier.create(virtualKeyService.createVirtualKey("uid", CxTypeAuthFleetDto.PG, "cxId", Mono.just(requestDto)))
                .verifyErrorMatches(throwable -> throwable instanceof RuntimeException && throwable.getMessage().equals("Internal error"));
    }

    @Test
    void createVirtualKey_virtualKeyAlreadyExists() {
        RequestNewVirtualKeyDto requestDto = new RequestNewVirtualKeyDto();

        ApiKeyModel apiKeyModel = new ApiKeyModel();
        apiKeyModel.setId("id");
        apiKeyModel.setUid("uid");
        apiKeyModel.setCxId("cxId");
        apiKeyModel.setScope(ApiKeyModel.Scope.CLIENTID);
        apiKeyModel.setStatus(VirtualKeyStatusDto.ENABLED.getValue());

        Page<ApiKeyModel> page = Page.create(List.of(apiKeyModel));


        when(pnUserAttributesClient.getConsentByType(any(), any(), any(), any())).thenReturn(Mono.just(new ConsentDto().accepted(true)));
        when(apiKeyRepository.findByUidAndCxIdAndStatusAndScope(any(), any(), any(), any())).thenReturn(Mono.just(page));
        when(apiKeyRepository.save(any())).thenReturn(Mono.error(new RuntimeException("Internal error")));

        StepVerifier.create(virtualKeyService.createVirtualKey("uid", CxTypeAuthFleetDto.PG, "cxId", Mono.just(requestDto)))
                .verifyErrorMatches(throwable -> throwable instanceof ApiKeyManagerException && throwable.getMessage().equals("Virtual key with status ACTIVE already exists."));
    }

}