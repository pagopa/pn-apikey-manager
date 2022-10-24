package it.pagopa.pn.apikey.manager.config;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ContextConfiguration(classes = {ApiGatewayConfig.class})
@ExtendWith(SpringExtension.class)
class ApiGatewayConfigTest {
    @Autowired
    private ApiGatewayConfig apiGatewayConfig;

    /**
     * Method under test: {@link ApiGatewayConfig#apiGatewayAsync()}
     */
    @Test
    void testApiGatewayAsync() {
        Assertions.assertNotNull(this.apiGatewayConfig.apiGatewayAsync());
    }
}

