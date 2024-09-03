package it.pagopa.pn.apikey.manager.client;

import it.pagopa.pn.apikey.manager.apikey.manager.generated.openapi.msclient.pnuserattributes.v1.api.ConsentsApi;
import it.pagopa.pn.apikey.manager.apikey.manager.generated.openapi.msclient.pnuserattributes.v1.dto.ConsentDto;
import it.pagopa.pn.apikey.manager.apikey.manager.generated.openapi.msclient.pnuserattributes.v1.dto.ConsentTypeDto;
import it.pagopa.pn.apikey.manager.apikey.manager.generated.openapi.msclient.pnuserattributes.v1.dto.CxTypeAuthFleetDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

class PnUserAttributesClientTest {

    private ConsentsApi consentsApi;
    private PnUserAttributesClient pnUserAttributesClient;

    @BeforeEach
    void setUp() {
        consentsApi = Mockito.mock(ConsentsApi.class);
        pnUserAttributesClient = new PnUserAttributesClient(consentsApi);
    }

    @Test
    @DisplayName("Should return ConsentDto when getConsentByType is successful")
    void getConsentByTypeSuccess() {
        ConsentDto expectedConsent = new ConsentDto();
        when(consentsApi.getConsentByType(anyString(), any(CxTypeAuthFleetDto.class), any(ConsentTypeDto.class), anyString()))
                .thenReturn(Mono.just(expectedConsent));

        Mono<ConsentDto> result = pnUserAttributesClient.getConsentByType("uid", CxTypeAuthFleetDto.PG.getValue(), ConsentTypeDto.TOS, "v1");

        assertEquals(expectedConsent, result.block());
    }

    @Test
    @DisplayName("Should handle error when getConsentByType fails")
    void getConsentByTypeError() {
        when(consentsApi.getConsentByType(anyString(), any(CxTypeAuthFleetDto.class), any(ConsentTypeDto.class), anyString()))
                .thenReturn(Mono.error(new RuntimeException("Error")));

        StepVerifier.create(pnUserAttributesClient.getConsentByType("uid", CxTypeAuthFleetDto.PG.getValue(), ConsentTypeDto.TOS, "v1"))
                .verifyError();
    }
}