package it.pagopa.pn.apikey.manager.config;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ContextConfiguration(classes = {ApiGatewayConfig.class})
@TestPropertySource(properties = {
        "aws.region=eu-south-1"
})
@ExtendWith(SpringExtension.class)
class ApiGatewayConfigTest {

    @Autowired
    private ApiGatewayConfig apiGatewayConfig;

    @Test
    void testApiGatewayAsync() {
        Assertions.assertNotNull(apiGatewayConfig.apiGatewayAsync());
    }

}
