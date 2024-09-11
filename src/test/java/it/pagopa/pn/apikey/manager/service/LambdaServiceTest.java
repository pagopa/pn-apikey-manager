package it.pagopa.pn.apikey.manager.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.pagopa.pn.apikey.manager.config.LambdaAsyncConfig;
import it.pagopa.pn.apikey.manager.config.PnApikeyManagerConfig;
import it.pagopa.pn.apikey.manager.exception.ApiKeyManagerException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import software.amazon.awssdk.services.lambda.LambdaAsyncClient;
import software.amazon.awssdk.services.lambda.model.InvokeRequest;
import software.amazon.awssdk.services.lambda.model.InvokeResponse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
class LambdaServiceTest {

    private LambdaAsyncClient lambdaAsyncClient;

    private PnApikeyManagerConfig pnApikeyManagerConfig;

    private LambdaService lambdaService;


    @BeforeEach
    void setUp() {
        lambdaAsyncClient = mock(LambdaAsyncClient.class);
        pnApikeyManagerConfig = mock(PnApikeyManagerConfig.class);
        lambdaService = new LambdaService(lambdaAsyncClient, new ObjectMapper(), pnApikeyManagerConfig);
    }

    @Test
    void testInvokeLambda() throws Exception {
        // Arrange
        String functionName = "testFunction";
        String cxId = "testCxId";
        List<Map<String, Object>> jwksBody = new ArrayList<>();
        String actionType = "UPSERT";

        Map<String, Object> payload = new HashMap<>();
        payload.put("actionType", actionType);
        payload.put("iss", cxId);
        payload.put("attributeResolversCfgs", null);
        payload.put("JWKSCacheMaxDurationSec", 3600);
        payload.put("JWKSCacheRenewSec", 300);
        payload.put("JWKSBody", jwksBody);

        when(pnApikeyManagerConfig.getAttributeResolversCfgs()).thenReturn(null);
        when(pnApikeyManagerConfig.getJwksCacheMaxDurationSec()).thenReturn(3600);
        when(pnApikeyManagerConfig.getJwksCacheRenewSec()).thenReturn(300);

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
    void testInvokeLambdaWithError() throws Exception {
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