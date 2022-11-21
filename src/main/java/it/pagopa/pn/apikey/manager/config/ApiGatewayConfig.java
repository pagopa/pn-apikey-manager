package it.pagopa.pn.apikey.manager.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.apigateway.ApiGatewayAsyncClient;

@Configuration
public class ApiGatewayConfig {

    private final String awsRegion;

    public ApiGatewayConfig(@Value("${aws.region}") String awsRegion) {
        this.awsRegion = awsRegion;
    }

    @Bean
    public ApiGatewayAsyncClient apiGatewayAsync() {
        return ApiGatewayAsyncClient.builder()
                .region(Region.of(awsRegion))
                .build();
    }

}
