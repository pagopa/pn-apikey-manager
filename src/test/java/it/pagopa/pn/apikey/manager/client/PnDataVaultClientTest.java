package it.pagopa.pn.apikey.manager.client;

import it.pagopa.pn.apikey.manager.generated.openapi.msclient.pndatavault.v1.api.RecipientsApi;
import it.pagopa.pn.apikey.manager.generated.openapi.msclient.pndatavault.v1.dto.BaseRecipientDtoDto;
import it.pagopa.pn.apikey.manager.generated.openapi.msclient.pndatavault.v1.dto.RecipientTypeDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class PnDataVaultClientTest {

    @Mock
    private RecipientsApi recipientsApi;

    private PnDataVaultClient pnDataVaultClient;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        pnDataVaultClient = new PnDataVaultClient(recipientsApi);
    }

    @Test
    @DisplayName("Should return recipient denominations for valid internal IDs")
    void getRecipientDenominationByInternalId_withValidInternalIds() {
        BaseRecipientDtoDto recipient = new BaseRecipientDtoDto();
        recipient.setTaxId("taxId");
        recipient.setDenomination("denomination");

        when(recipientsApi.getRecipientDenominationByInternalId(any()))
                .thenReturn(Flux.just(recipient));

        StepVerifier.create(pnDataVaultClient.getRecipientDenominationByInternalId(List.of("internalId1", "internalId2")))
                .expectNext(recipient)
                .verifyComplete();
    }

    @Test
    @DisplayName("Should return empty flux when no internal IDs are provided")
    void getRecipientDenominationByInternalId_withNoInternalIds() {
        when(recipientsApi.getRecipientDenominationByInternalId(any()))
                .thenReturn(Flux.empty());

        StepVerifier.create(pnDataVaultClient.getRecipientDenominationByInternalId(List.of()))
                .verifyComplete();
    }

    @Test
    @DisplayName("Should return iuid for valid fiscal code and person type")
    void ensureRecipientByExternalId_withValidFiscalCodeAndPersonType() {
        when(recipientsApi.ensureRecipientByExternalId(RecipientTypeDto.PF, "fiscalCode"))
                .thenReturn(Mono.just("iuid"));

        StepVerifier.create(pnDataVaultClient.ensureRecipientByExternalId(true, "fiscalCode"))
                .expectNext("iuid")
                .verifyComplete();
    }

    @Test
    @DisplayName("Should return iuid for valid fiscal code and company type")
    void ensureRecipientByExternalId_withValidFiscalCodeAndCompanyType() {
        when(recipientsApi.ensureRecipientByExternalId(RecipientTypeDto.PG, "fiscalCode"))
                .thenReturn(Mono.just("iuid"));

        StepVerifier.create(pnDataVaultClient.ensureRecipientByExternalId(false, "fiscalCode"))
                .expectNext("iuid")
                .verifyComplete();
    }

    @Test
    @DisplayName("Should return empty mono when fiscal code is invalid")
    void ensureRecipientByExternalId_withInvalidFiscalCode() {
        when(recipientsApi.ensureRecipientByExternalId(any(), any()))
                .thenReturn(Mono.empty());

        StepVerifier.create(pnDataVaultClient.ensureRecipientByExternalId(true, "invalidFiscalCode"))
                .verifyComplete();
    }
}