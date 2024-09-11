package it.pagopa.pn.apikey.manager.config;

import it.pagopa.pn.commons.configs.aws.AwsConfigs;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.awscore.client.builder.AwsClientBuilder;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.lambda.LambdaAsyncClient;

import java.net.URI;

@Configuration
@RequiredArgsConstructor
public class LambdaAsyncConfig {

    private final AwsConfigs props;

    @Bean
    public LambdaAsyncClient lambdaAsyncClient() {
        return (LambdaAsyncClient) this.configureBuilder(LambdaAsyncClient.builder());
    }

    private <C> C configureBuilder(AwsClientBuilder<?, C> builder) {
        if (this.props != null) {
            String profileName = this.props.getProfileName();
            if (StringUtils.isNotBlank(profileName)) {
                builder.credentialsProvider(ProfileCredentialsProvider.create(profileName));
            }

            String regionCode = this.props.getRegionCode();
            if (StringUtils.isNotBlank(regionCode)) {
                builder.region(Region.of(regionCode));
            }

            String endpointUrl = this.props.getEndpointUrl();
            if (StringUtils.isNotBlank(endpointUrl)) {
                builder.endpointOverride(URI.create(endpointUrl));
            }
        }

        return builder.build();
    }
}
