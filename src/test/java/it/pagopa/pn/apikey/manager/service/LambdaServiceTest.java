package it.pagopa.pn.apikey.manager.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.pagopa.pn.apikey.manager.config.PnApikeyManagerConfig;
import it.pagopa.pn.apikey.manager.exception.ApiKeyManagerException;
import it.pagopa.pn.apikey.manager.utils.PublicKeyUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import software.amazon.awssdk.services.lambda.LambdaAsyncClient;
import software.amazon.awssdk.services.lambda.model.InvokeRequest;
import software.amazon.awssdk.services.lambda.model.InvokeResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@EnableConfigurationProperties(value = PnApikeyManagerConfig.class)
@TestPropertySource("classpath:application-test.properties")
class LambdaServiceTest {

    @MockBean
    private LambdaAsyncClient lambdaAsyncClient;

    @Autowired
    private PnApikeyManagerConfig pnApikeyManagerConfig;

    private LambdaService lambdaService;

    @BeforeEach
    void setUp() {
        lambdaService = new LambdaService(lambdaAsyncClient, new ObjectMapper(), pnApikeyManagerConfig);
    }

    @Test
    void testInvokeLambda() {
        // Arrange
        String functionName = "testFunction";
        String cxId = "testCxId";

        String pemKey = "MEgCQQCbhj5JBrKCY5RT9NEJ80MedgWwF0RF/hfrl+AA53VycL3XXzGqDqUj13wy\n" +
                "Y2T9heP2O/kHK8t91xSxz9+sDeDNAgMBAAE=";
        Map<String, Object> jwk1 = PublicKeyUtils.createJWKFromData(pemKey, "exponent1", "kid1", "RS256");

        List<Map<String, Object>> jwksBody = List.of(jwk1);

        InvokeResponse invokeResponse = InvokeResponse.builder().statusCode(200).logResult("logResult").build();
        CompletableFuture<InvokeResponse> future = CompletableFuture.completedFuture(invokeResponse);
        when(lambdaAsyncClient.invoke(any(InvokeRequest.class))).thenReturn(future);

        // Act
        Mono<Void> result = lambdaService.invokeLambda(functionName, cxId, jwksBody);

        // Assert
        StepVerifier.create(result)
                .verifyComplete();

        verify(lambdaAsyncClient, times(1)).invoke(any(InvokeRequest.class));
    }

    @Test
    void testInvokeLambdaWithError() {
        // Arrange
        String functionName = "testFunction";
        String cxId = "testCxId";
        List<Map<String, Object>> jwksBody = new ArrayList<>();

        // Act
        Mono<Void> result = lambdaService.invokeLambda(functionName, cxId, jwksBody);

        // Assert
        StepVerifier.create(result)
                .verifyError(ApiKeyManagerException.class);
    }
}