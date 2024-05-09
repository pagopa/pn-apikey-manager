package it.pagopa.pn.apikey.manager.config;

import it.pagopa.pn.apikey.manager.log.AwsClientLoggerInterceptor;
import it.pagopa.pn.commons.configs.aws.AwsConfigs;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.core.client.config.ClientOverrideConfiguration;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedAsyncClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;

@Configuration
@ConfigurationProperties("aws")
public class AwsConfigsActivation extends AwsConfigs {

}
