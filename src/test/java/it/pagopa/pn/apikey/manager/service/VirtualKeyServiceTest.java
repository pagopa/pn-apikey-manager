package it.pagopa.pn.apikey.manager.service;

import it.pagopa.pn.apikey.manager.apikey.manager.generated.openapi.msclient.pnexternalregistries.v1.dto.PrivacyNoticeVersionResponseDto;
import it.pagopa.pn.apikey.manager.apikey.manager.generated.openapi.msclient.pnuserattributes.v1.dto.ConsentDto;
import it.pagopa.pn.apikey.manager.client.PnExternalRegistriesClient;
import it.pagopa.pn.apikey.manager.client.PnUserAttributesClient;
import it.pagopa.pn.apikey.manager.config.PnApikeyManagerConfig;
import it.pagopa.pn.apikey.manager.entity.ApiKeyModel;
import it.pagopa.pn.apikey.manager.exception.ApiKeyManagerException;
import it.pagopa.pn.apikey.manager.generated.openapi.server.v1.dto.CxTypeAuthFleetDto;
import it.pagopa.pn.apikey.manager.generated.openapi.server.v1.dto.RequestNewVirtualKeyDto;
import it.pagopa.pn.apikey.manager.generated.openapi.server.v1.dto.VirtualKeyStatusDto;
import it.pagopa.pn.apikey.manager.repository.ApiKeyRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.PropertySource;
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
@PropertySource("classpath:application-test.properties")
@EnableConfigurationProperties
class VirtualKeyServiceTest {

    @Autowired
    private VirtualKeyService virtualKeyService;

    @MockBean
    private DynamoDbEnhancedAsyncClient dynamoDbEnhancedAsyncClient;

    @MockBean
    private PnUserAttributesClient pnUserAttributesClient;

    @MockBean
    private PnExternalRegistriesClient pnExternalRegistriesClient;

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

        PrivacyNoticeVersionResponseDto versionDto = new PrivacyNoticeVersionResponseDto();
        versionDto.setVersion(1);

        when(pnUserAttributesClient.getPgConsentByType(any(), any(), any(), any(), any(), any())).thenReturn(Mono.just(new ConsentDto().accepted(true)));
        when(apiKeyRepository.findByUidAndCxIdAndStatusAndScope(any(), any(), any(), any())).thenReturn(Mono.empty());
        when(apiKeyRepository.save(any())).thenReturn(Mono.just(apiKeyModel));
        when(pnExternalRegistriesClient.findPrivacyNoticeVersion(any(), any())).thenReturn(Mono.just(versionDto));

        StepVerifier.create(virtualKeyService.createVirtualKey("uid", CxTypeAuthFleetDto.PG, "cxId", Mono.just(requestDto), "ADMN", null))
                .expectNextMatches(response -> response.getId().equals("id") && response.getVirtualKey().equals("virtualKey"))
                .verifyComplete();
    }

    @Test
    void createVirtualKey_CxTypeNotAllowed() {
        RequestNewVirtualKeyDto requestDto = new RequestNewVirtualKeyDto();

        StepVerifier.create(virtualKeyService.createVirtualKey("uid", CxTypeAuthFleetDto.PA, "cxId", Mono.just(requestDto), "ADMN", null))
                .verifyErrorMatches(throwable -> throwable instanceof ApiKeyManagerException && throwable.getMessage().contains("CxTypeAuthFleet PA not allowed"));
    }

    @Test
    void createVirtualKey_TosConsentNotFound() {
        RequestNewVirtualKeyDto requestDto = new RequestNewVirtualKeyDto();
        PrivacyNoticeVersionResponseDto versionDto = new PrivacyNoticeVersionResponseDto();
        versionDto.setVersion(1);

        when(apiKeyRepository.findByUidAndCxIdAndStatusAndScope(any(), any(), any(), any())).thenReturn(Mono.empty());
        when(pnUserAttributesClient.getPgConsentByType(any(), any(), any(), any(), any(), any())).thenReturn(Mono.just(new ConsentDto().accepted(false)));
        when(pnExternalRegistriesClient.findPrivacyNoticeVersion(any(), any())).thenReturn(Mono.just(versionDto));

        StepVerifier.create(virtualKeyService.createVirtualKey("uid", CxTypeAuthFleetDto.PG, "cxId", Mono.just(requestDto), "ADMN", null))
                .verifyErrorMatches(throwable -> throwable instanceof ApiKeyManagerException && throwable.getMessage().contains("TOS DEST B2B consent not found"));
    }

    @Test
    void createVirtualKey_InternalError() {
        RequestNewVirtualKeyDto requestDto = new RequestNewVirtualKeyDto();
        PrivacyNoticeVersionResponseDto versionDto = new PrivacyNoticeVersionResponseDto();
        versionDto.setVersion(1);

        when(pnUserAttributesClient.getPgConsentByType(any(), any(), any(), any(), any(), any())).thenReturn(Mono.just(new ConsentDto().accepted(true)));
        when(apiKeyRepository.findByUidAndCxIdAndStatusAndScope(any(), any(), any(), any())).thenReturn(Mono.empty());
        when(apiKeyRepository.save(any())).thenReturn(Mono.error(new RuntimeException("Internal error")));
        when(pnExternalRegistriesClient.findPrivacyNoticeVersion(any(), any())).thenReturn(Mono.just(versionDto));

        StepVerifier.create(virtualKeyService.createVirtualKey("uid", CxTypeAuthFleetDto.PG, "cxId", Mono.just(requestDto), "ADMN", null))
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

        PrivacyNoticeVersionResponseDto versionDto = new PrivacyNoticeVersionResponseDto();
        versionDto.setVersion(1);

        when(pnUserAttributesClient.getPgConsentByType(any(), any(), any(), any(), any(), any())).thenReturn(Mono.just(new ConsentDto().accepted(true)));
        when(apiKeyRepository.findByUidAndCxIdAndStatusAndScope(any(), any(), any(), any())).thenReturn(Mono.just(page));
        when(apiKeyRepository.save(any())).thenReturn(Mono.error(new RuntimeException("Internal error")));
        when(pnExternalRegistriesClient.findPrivacyNoticeVersion(any(), any())).thenReturn(Mono.just(versionDto));

        StepVerifier.create(virtualKeyService.createVirtualKey("uid", CxTypeAuthFleetDto.PG, "cxId", Mono.just(requestDto), "ADMN", null))
                .verifyErrorMatches(throwable -> throwable instanceof ApiKeyManagerException && throwable.getMessage().equals("Virtual key with status ENABLED already exists."));
    }

}