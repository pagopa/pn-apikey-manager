package it.pagopa.pn.apikey.manager.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedAsyncClient;

@Configuration
public class DynamoConfiguration {
    @Bean
    public DynamoDbEnhancedAsyncClient dynamoDb(){
        return DynamoDbEnhancedAsyncClient.builder().build();
    }
}
