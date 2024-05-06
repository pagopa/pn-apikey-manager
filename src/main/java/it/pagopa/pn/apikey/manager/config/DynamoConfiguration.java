package it.pagopa.pn.apikey.manager.config;

import it.pagopa.pn.apikey.manager.log.AwsClientLoggerInterceptor;
import java.net.URI;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.client.config.ClientOverrideConfiguration;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedAsyncClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

@Slf4j
//@Configuration
public class DynamoConfiguration {

    private final String awsRegion;

    public DynamoConfiguration(@Value("${aws.region}") String awsRegion) {
        this.awsRegion = awsRegion;
    }


    @Primary
    @Bean
    public DynamoDbEnhancedAsyncClient dynamoDb() {
        DynamoDbAsyncClient asyncClient = DynamoDbAsyncClient.builder()
                .region(Region.of(awsRegion))
                .credentialsProvider(DefaultCredentialsProvider.builder().build())
                .overrideConfiguration(ClientOverrideConfiguration.builder()
                        .addExecutionInterceptor(new AwsClientLoggerInterceptor())
                        .build())
                .build();
        return DynamoDbEnhancedAsyncClient.builder()
                .dynamoDbClient(asyncClient)
                .build();
    }



//    @Bean
//    public DynamoDbAsyncClient getDynamoDbClient() {
//        return DynamoDbAsyncClient.builder()
//            .endpointOverride(URI.create("http://localhost:4566/"))
//            .region(Region.US_EAST_1)
//            .credentialsProvider(StaticCredentialsProvider.create(
//                AwsBasicCredentials.create("test", "test")))
//            .build();
//    }
//
//    @Bean
//    @Primary
//    public DynamoDbEnhancedAsyncClient getDynamoDbEnhancedClient() {
//        return DynamoDbEnhancedAsyncClient.builder()
//            .dynamoDbClient(getDynamoDbClient())
//            .build();
//    }
}
