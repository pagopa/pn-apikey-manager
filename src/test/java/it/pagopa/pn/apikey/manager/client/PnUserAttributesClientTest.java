package it.pagopa.pn.apikey.manager.client;

import it.pagopa.pn.apikey.manager.apikey.manager.generated.openapi.msclient.pnuserattributes.v1.api.ConsentsApi;
import it.pagopa.pn.apikey.manager.apikey.manager.generated.openapi.msclient.pnuserattributes.v1.dto.ConsentDto;
import it.pagopa.pn.apikey.manager.apikey.manager.generated.openapi.msclient.pnuserattributes.v1.dto.ConsentTypeDto;
import it.pagopa.pn.apikey.manager.apikey.manager.generated.openapi.msclient.pnuserattributes.v1.dto.CxTypeAuthFleetDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
class PnUserAttributesClientTest {

    private ConsentsApi consentsApi;
    private PnUserAttributesClient pnUserAttributesClient;

    @BeforeEach
    void setUp() {
        consentsApi = Mockito.mock(ConsentsApi.class);
        pnUserAttributesClient = new PnUserAttributesClient(consentsApi);
    }

    @Test
    @DisplayName("Should return ConsentDto when getPgConsentByType is successful")
    void getConsentByTypeSuccess() {
        ConsentDto expectedConsent = new ConsentDto();
        when(consentsApi.getPgConsentByType(anyString(), any(CxTypeAuthFleetDto.class), anyString(), any(ConsentTypeDto.class), any(), anyString()))
                .thenReturn(Mono.just(expectedConsent));

        Mono<ConsentDto> result = pnUserAttributesClient.getPgConsentByType("uid", CxTypeAuthFleetDto.PG.getValue(), "ADMIN", ConsentTypeDto.TOS_DEST_B2B, null, "v1");

        assertEquals(expectedConsent, result.block());
    }

    @Test
    @DisplayName("Should handle error when getPgConsentByType fails")
    void getConsentByTypeError() {
        when(consentsApi.getPgConsentByType(anyString(), any(CxTypeAuthFleetDto.class), anyString(), any(ConsentTypeDto.class), any(), anyString()))
                .thenReturn(Mono.error(new RuntimeException("Error")));

        StepVerifier.create(pnUserAttributesClient.getPgConsentByType("uid", CxTypeAuthFleetDto.PG.getValue(), "USER", ConsentTypeDto.TOS_DEST_B2B, null, "v1"))
                .verifyError();
    }
}