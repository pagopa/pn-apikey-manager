package it.pagopa.pn.apikey.manager.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedAsyncClient;

@Slf4j
@Configuration
public class DynamoConfiguration {

    @Bean
    public DynamoDbEnhancedAsyncClient amazonDynamoDB() {
        return DynamoDbEnhancedAsyncClient.builder().build();
    }

}

