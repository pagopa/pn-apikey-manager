package it.pagopa.pn.apikey.manager.client;

import it.pagopa.pn.apikey.manager.apikey.manager.generated.openapi.msclient.pnexternalregistries.v1.api.InternalOnlyApi;
import it.pagopa.pn.apikey.manager.apikey.manager.generated.openapi.msclient.pnexternalregistries.v1.api.PrivacyNoticeApi;
import it.pagopa.pn.apikey.manager.apikey.manager.generated.openapi.msclient.pnexternalregistries.v1.dto.PgUserDetailDto;
import it.pagopa.pn.apikey.manager.apikey.manager.generated.openapi.msclient.pnexternalregistries.v1.dto.PrivacyNoticeVersionResponseDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
class PnExternalRegistriesClientTest {

    private PrivacyNoticeApi privacyNoticeApi;
    private InternalOnlyApi internalOnlyApi;
    private PnExternalRegistriesClient pnExternalRegistriesClient;

    @BeforeEach
    void setUp() {
        privacyNoticeApi = Mockito.mock(PrivacyNoticeApi.class);
        internalOnlyApi = Mockito.mock(InternalOnlyApi.class);
        pnExternalRegistriesClient = new PnExternalRegistriesClient(privacyNoticeApi, internalOnlyApi);
    }

    @Test
    @DisplayName("Should return PrivacyNoticeVersionResponseDto when findPrivacyNoticeVersion is successful")
    void findPrivacyNoticeVersionSuccessful() {
        PrivacyNoticeVersionResponseDto responseDto = new PrivacyNoticeVersionResponseDto();
        when(privacyNoticeApi.findPrivacyNoticeVersion(anyString(), anyString())).thenReturn(Mono.just(responseDto));

        Mono<PrivacyNoticeVersionResponseDto> result = pnExternalRegistriesClient.findPrivacyNoticeVersion("consentType", "portalType");

        StepVerifier.create(result)
                .expectNext(responseDto)
                .verifyComplete();
    }

    @Test
    @DisplayName("Should return error when findPrivacyNoticeVersion fails")
    void findPrivacyNoticeVersionFails() {
        when(privacyNoticeApi.findPrivacyNoticeVersion(anyString(), anyString())).thenReturn(Mono.error(new RuntimeException("Error")));

        Mono<PrivacyNoticeVersionResponseDto> result = pnExternalRegistriesClient.findPrivacyNoticeVersion("consentType", "portalType");

        StepVerifier.create(result)
                .expectErrorMessage("Error")
                .verify();
    }

    @Test
    @DisplayName("Should return empty when findPrivacyNoticeVersion returns empty")
    void findPrivacyNoticeVersionReturnsEmpty() {
        when(privacyNoticeApi.findPrivacyNoticeVersion(anyString(), anyString())).thenReturn(Mono.empty());

        Mono<PrivacyNoticeVersionResponseDto> result = pnExternalRegistriesClient.findPrivacyNoticeVersion("consentType", "portalType");

        StepVerifier.create(result)
                .verifyComplete();
    }

    @Test
    @DisplayName("Should return PgUserDetailDto when getPgUsersDetailsPrivate is successful")
    void getPgUsersDetailsPrivateSuccessful() {
        PgUserDetailDto responseDto = new PgUserDetailDto();
        when(internalOnlyApi.getPgUsersDetailsPrivate(anyString(), anyString())).thenReturn(Mono.just(responseDto));

        Mono<PgUserDetailDto> result = pnExternalRegistriesClient.getPgUsersDetailsPrivate("xPagopaPnUid", "xPagopaPnCxId");

        StepVerifier.create(result)
                .expectNext(responseDto)
                .verifyComplete();
    }
}