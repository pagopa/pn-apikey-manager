package it.pagopa.pn.apikey.manager.config;

import org.apache.http.client.CredentialsProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.apigateway.ApiGatewayAsyncClient;

@Configuration
public class ApiGatewayConfig {

    private final String awsRegion;
    private final String awsProfileName;

    public ApiGatewayConfig(@Value("${aws.region}") String awsRegion, @Value("${aws.profile-name}") String awsPofileName) {
        this.awsRegion = awsRegion;
        this.awsProfileName = awsPofileName;
    }

    @Bean
    public ApiGatewayAsyncClient apiGatewayAsync() {
        return ApiGatewayAsyncClient.builder()
                .region(Region.of(awsRegion))
                .credentialsProvider(
                    DefaultCredentialsProvider.builder()
                    .profileName(awsProfileName)
                    .build())
                .build();
    }

}
