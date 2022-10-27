package it.pagopa.pn.apikey.manager.config;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ContextConfiguration(classes = {DynamoConfiguration.class})
@ExtendWith(SpringExtension.class)
class DynamoConfigurationTest {

    @Autowired
    private DynamoConfiguration dynamoConfiguration;

    /**
     * Method under test: {@link DynamoConfiguration#dynamoDb()}
     */
    @Test
    void testDynamoDb() {
        Assertions.assertNotNull(this.dynamoConfiguration.dynamoDb());
    }
}
