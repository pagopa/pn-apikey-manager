package it.pagopa.pn.apikey.manager.config;

import it.pagopa.pn.commons.configs.aws.AwsConfigs;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import software.amazon.awssdk.services.lambda.LambdaAsyncClient;

import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
class LambdaAsyncConfigTest {

    @Mock
    private AwsConfigs awsConfigs;

    @InjectMocks
    private LambdaAsyncConfig lambdaAsyncConfig;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @Disabled
    void testLambdaAsyncClient() {
        // Arrange
        when(awsConfigs.getProfileName()).thenReturn("default");
        when(awsConfigs.getRegionCode()).thenReturn("eu-south-1");
        when(awsConfigs.getEndpointUrl()).thenReturn("http://localhost:4556");

        // Act
        LambdaAsyncClient lambdaAsyncClient = lambdaAsyncConfig.lambdaAsyncClient();

        // Assert
        Assertions.assertNotNull(lambdaAsyncClient);
    }
}