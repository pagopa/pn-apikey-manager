package it.pagopa.pn.apikey.manager.service;

import it.pagopa.pn.apikey.manager.config.PnApikeyManagerConfig;
import it.pagopa.pn.apikey.manager.entity.ApiKeyAggregateModel;
import it.pagopa.pn.apikey.manager.generated.openapi.rest.v1.aggregate.dto.AggregateRequestDto;
import it.pagopa.pn.apikey.manager.generated.openapi.rest.v1.aggregate.dto.SaveAggregateResponseDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.test.StepVerifier;
import software.amazon.awssdk.services.apigateway.ApiGatewayAsyncClient;
import software.amazon.awssdk.services.apigateway.model.*;

import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ContextConfiguration(classes = {ApiGatewayService.class})
@ExtendWith(SpringExtension.class)
class ApiGatewayServiceTest {

    @Autowired
    ApiGatewayService apiGatewayService;

    @MockBean
    ApiGatewayAsyncClient apiGatewayAsyncClient;

    @MockBean
    PnApikeyManagerConfig pnApikeyManagerConfig;

    @Test
    void moveApiKeyToNewUsagePlan() {
        AggregateRequestDto dto = new AggregateRequestDto();
        dto.setName("name");
        dto.setDescription("description");
        dto.setUsagePlanId("usagePlanId");

        ApiKeyAggregateModel model = new ApiKeyAggregateModel();
        model.setAggregateId("id");

        CompletableFuture<DeleteUsagePlanKeyResponse> completableFuture = new CompletableFuture<>();
        completableFuture.completeAsync(() -> DeleteUsagePlanKeyResponse.builder().build());
        when(apiGatewayAsyncClient.deleteUsagePlanKey((DeleteUsagePlanKeyRequest) any()))
                .thenReturn(completableFuture);

        CompletableFuture<CreateUsagePlanKeyResponse> completableFuture2 = new CompletableFuture<>();
        completableFuture2.completeAsync(()-> CreateUsagePlanKeyResponse.builder().build());
        when(apiGatewayAsyncClient.createUsagePlanKey((CreateUsagePlanKeyRequest) any()))
                .thenReturn(completableFuture2);
        CreateUsagePlanKeyResponse createUsagePlanKeyResponse = CreateUsagePlanKeyResponse.builder().build();

        StepVerifier.create(apiGatewayService.moveApiKeyToNewUsagePlan(model,dto))
                .expectNext(createUsagePlanKeyResponse).verifyComplete();
    }

    @Test
    void testCreateNewAwsApiKey() {
        CreateApiKeyResponse createApiKeyResponse = CreateApiKeyResponse.builder().name("test").id("id").build();
        CompletableFuture<CreateApiKeyResponse> completableFuture = new CompletableFuture<>();
        completableFuture.completeAsync(() -> createApiKeyResponse);
        when(apiGatewayAsyncClient.createApiKey((CreateApiKeyRequest) any())).thenReturn(completableFuture);

        CreateUsagePlanResponse createUsagePlanResponse = CreateUsagePlanResponse.builder().id("id").build();
        CompletableFuture<CreateUsagePlanResponse> completableFuture1 = new CompletableFuture<>();
        completableFuture1.completeAsync(() -> createUsagePlanResponse);

        CreateUsagePlanKeyResponse createUsagePlanKeyResponse = CreateUsagePlanKeyResponse.builder().id("id").build();
        CompletableFuture<CreateUsagePlanKeyResponse> completableFuture2 = new CompletableFuture<>();
        completableFuture2.completeAsync(() -> createUsagePlanKeyResponse);

        when(apiGatewayAsyncClient.createUsagePlan((CreateUsagePlanRequest) any())).thenReturn(completableFuture1);
        when(apiGatewayAsyncClient.createUsagePlanKey((CreateUsagePlanKeyRequest) any())).thenReturn(completableFuture2);

        StepVerifier.create(apiGatewayService.createNewAwsApiKey("Pa"))
                .expectNext(CreateApiKeyResponse.builder().name("test").id("id").build()).verifyComplete();
    }
}
