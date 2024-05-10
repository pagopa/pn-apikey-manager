package it.pagopa.pn.apikey.manager.config;

import java.net.URI;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.apigateway.ApiGatewayAsyncClient;
import software.amazon.awssdk.services.apigateway.ApiGatewayAsyncClientBuilder;

@Configuration
public class ApiGatewayConfig {

    private final String awsRegion;
    private final String awsProfileName;
    private final String awsEndpoint;

    public ApiGatewayConfig(@Value("${aws.region-code}") String awsRegion
        , @Value("${aws.profile-name}") String awsPofileName
        ,@Value("${aws.endpoint-url:#{null}}") String awsEndpoint) {
        this.awsRegion = awsRegion;
        this.awsProfileName = awsPofileName;
        this.awsEndpoint = awsEndpoint;
    }

    @Bean
    public ApiGatewayAsyncClient apiGatewayAsync() {
        ApiGatewayAsyncClientBuilder clientBuilder = ApiGatewayAsyncClient.builder()
            .region(Region.of(awsRegion))
            .credentialsProvider(
                DefaultCredentialsProvider.builder()
                    .profileName(awsProfileName)
                    .build()
            );

        if (awsEndpoint != null) {
            clientBuilder = clientBuilder.endpointOverride(URI.create(awsEndpoint));
        }

        return clientBuilder.build();
    }

}
