package it.pagopa.pn.apikey.manager.client;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ExternalRegistriesWebClientTest {

    @Test
    void testInit(){
        ExternalRegistriesWebClient externalRegistriesWebClient = new ExternalRegistriesWebClient (100,100,
                100,100,"test.it");
        Assertions.assertThrows(NullPointerException.class, externalRegistriesWebClient::init);
    }


}
