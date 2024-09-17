package it.pagopa.pn.apikey.manager.service;

import it.pagopa.pn.apikey.manager.config.PnApikeyManagerConfig;
import it.pagopa.pn.apikey.manager.exception.ApiKeyManagerException;
import it.pagopa.pn.apikey.manager.utils.PublicKeyUtils;
import lombok.CustomLog;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.lambda.LambdaAsyncClient;
import software.amazon.awssdk.services.lambda.model.InvokeRequest;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static it.pagopa.pn.apikey.manager.exception.ApiKeyManagerExceptionError.FAILED_TO_INVOKE_LAMBDA_FUNCTION;

@CustomLog
@Service
@RequiredArgsConstructor
public class LambdaService {

    private final LambdaAsyncClient lambdaAsyncClient;
    private final ObjectMapper objectMapper;
    private final PnApikeyManagerConfig pnApikeyManagerConfig;

    public Mono<Void> invokeLambda(String functionName, String cxId, List<Map<String, Object>> jwksBody) {
        String actionType = (jwksBody == null || jwksBody.isEmpty()) ? "DELETE" : "UPSERT";

        Map<String, Object> payload = new HashMap<>();
        payload.put("actionType", actionType);
        payload.put("iss", cxId);

        payload.put("attributeResolversCfgs", pnApikeyManagerConfig.retrieveAttributeResolvers());

        payload.put("JWKSCacheMaxDurationSec", pnApikeyManagerConfig.getJwksCacheMaxDurationSec());
        payload.put("JWKSCacheRenewSec", pnApikeyManagerConfig.getJwksCacheRenewSec());
        payload.put("JWKSBody", PublicKeyUtils.createJWKSJson(jwksBody));

        try {
            String jsonPayload = objectMapper.writeValueAsString(payload);

            InvokeRequest request = InvokeRequest.builder()
                    .functionName(functionName)
                    .payload(SdkBytes.fromUtf8String(jsonPayload))
                    .build();

            return Mono.fromFuture(lambdaAsyncClient.invoke(request))
                    .doOnNext(response -> log.info("Lambda function invoked with status code: {} and logResult: {}", response.statusCode(), response.logResult()))
                    .then();
        } catch (Exception e) {
            return Mono.error(new ApiKeyManagerException(FAILED_TO_INVOKE_LAMBDA_FUNCTION, HttpStatus.INTERNAL_SERVER_ERROR));
        }
    }
}
