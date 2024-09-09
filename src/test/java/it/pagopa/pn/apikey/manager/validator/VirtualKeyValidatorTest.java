package it.pagopa.pn.apikey.manager.validator;

import it.pagopa.pn.apikey.manager.apikey.manager.generated.openapi.msclient.pnexternalregistries.v1.dto.PrivacyNoticeVersionResponseDto;
import it.pagopa.pn.apikey.manager.apikey.manager.generated.openapi.msclient.pnuserattributes.v1.dto.ConsentDto;
import it.pagopa.pn.apikey.manager.client.PnExternalRegistriesClient;
import it.pagopa.pn.apikey.manager.client.PnUserAttributesClient;
import it.pagopa.pn.apikey.manager.entity.ApiKeyModel;
import it.pagopa.pn.apikey.manager.entity.PublicKeyModel;
import it.pagopa.pn.apikey.manager.exception.ApiKeyManagerException;
import it.pagopa.pn.apikey.manager.generated.openapi.server.v1.dto.ApiKeyStatusDto;
import it.pagopa.pn.apikey.manager.generated.openapi.server.v1.dto.CxTypeAuthFleetDto;
import it.pagopa.pn.apikey.manager.generated.openapi.server.v1.dto.VirtualKeyStatusDto;
import it.pagopa.pn.apikey.manager.repository.ApiKeyRepository;
import it.pagopa.pn.apikey.manager.repository.PublicKeyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedAsyncClient;
import software.amazon.awssdk.enhanced.dynamodb.model.Page;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@SpringBootTest
@ExtendWith(SpringExtension.class)
class VirtualKeyValidatorTest {

    @Autowired
    private VirtualKeyValidator validator;
    @MockBean
    private DynamoDbEnhancedAsyncClient dynamoDbEnhancedAsyncClient;
    @MockBean
    private ApiKeyRepository apiKeyRepository;
    @MockBean
    private PublicKeyRepository publicKeyRepository;
    @MockBean
    private PnUserAttributesClient pnUserAttributesClient;
    @MockBean
    private PnExternalRegistriesClient pnExternalRegistriesClient;

    @BeforeEach
    void setUp() {
        validator = new VirtualKeyValidator(apiKeyRepository, publicKeyRepository, pnUserAttributesClient, pnExternalRegistriesClient);
    }

    @Test
    void validateTosAndValidPublicKey_shouldReturnError_whenTosConsentNotFound() {
        when(pnExternalRegistriesClient.findPrivacyNoticeVersion(any(), any()))
                .thenReturn(Mono.just(new PrivacyNoticeVersionResponseDto().version(1)));
        when(pnUserAttributesClient.getPgConsentByType(any(), any(), any(), any(), any(), any()))
                .thenReturn(Mono.just(new ConsentDto().accepted(false)));
        when(publicKeyRepository.findByCxIdAndWithoutTtl(anyString()))
                .thenReturn(Mono.just(Page.create(List.of())));

        Mono<Void> result = validator.validateTosAndValidPublicKey("testCxId", "testUid", CxTypeAuthFleetDto.PG, "USER", List.of("group1"));

        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof ApiKeyManagerException &&
                        ((ApiKeyManagerException) throwable).getStatus() == HttpStatus.FORBIDDEN)
                .verify();
    }

    @Test
    void validateTosAndValidPublicKey_shouldReturnError_whenPublicKeyNotFound() {
        when(pnExternalRegistriesClient.findPrivacyNoticeVersion(anyString(), anyString()))
                .thenReturn(Mono.just(new PrivacyNoticeVersionResponseDto().version(1)));
        when(pnUserAttributesClient.getPgConsentByType(anyString(), anyString(), anyString(), any(), any(), anyString()))
                .thenReturn(Mono.just(new ConsentDto().accepted(true)));
        when(publicKeyRepository.findByCxIdAndWithoutTtl(anyString()))
                .thenReturn(Mono.just(Page.create(List.of())));

        Mono<Void> result = validator.validateTosAndValidPublicKey("testCxId", "testUid", CxTypeAuthFleetDto.PG, "USER", List.of("group1"));

        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof ApiKeyManagerException &&
                        ((ApiKeyManagerException) throwable).getStatus() == HttpStatus.FORBIDDEN)
                .verify();
    }

    @Test
    void validateTosAndValidPublicKey_shouldComplete_whenTosConsentAndPublicKeyAreValid() {
        when(pnExternalRegistriesClient.findPrivacyNoticeVersion(anyString(), anyString()))
                .thenReturn(Mono.just(new PrivacyNoticeVersionResponseDto().version(1)));
        when(pnUserAttributesClient.getPgConsentByType(anyString(), anyString(), anyString(), any(), any(), anyString()))
                .thenReturn(Mono.just(new ConsentDto().accepted(true)));
        PublicKeyModel activeKey = new PublicKeyModel();
        activeKey.setStatus("ACTIVE");
        when(publicKeyRepository.findByCxIdAndWithoutTtl(anyString()))
                .thenReturn(Mono.just(Page.create(List.of(activeKey))));

        Mono<Void> result = validator.validateTosAndValidPublicKey("testCxId", "testUid", CxTypeAuthFleetDto.PG, "USER", List.of("group1"));

        StepVerifier.create(result)
                .verifyComplete();
    }
}
