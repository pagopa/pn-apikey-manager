package it.pagopa.pn.apikey.manager.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.apigateway.ApiGatewayAsyncClient;

@Configuration
public class ApiGatewayConfig {

    @Bean
    public ApiGatewayAsyncClient apiGatewayAsync(){
        return ApiGatewayAsyncClient.builder().region(Region.EU_SOUTH_1).build();
    }
}
