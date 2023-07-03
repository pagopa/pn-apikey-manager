package it.pagopa.pn.apikey.manager.service;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;

import it.pagopa.pn.apikey.manager.config.PnApikeyManagerConfig;

import java.util.*;
import java.util.concurrent.CompletableFuture;

import it.pagopa.pn.apikey.manager.generated.openapi.server.v1.aggregate.dto.UsagePlanDetailDto;
import it.pagopa.pn.apikey.manager.generated.openapi.server.v1.aggregate.dto.UsagePlanResponseDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.test.StepVerifier;
import software.amazon.awssdk.services.apigateway.ApiGatewayAsyncClient;
import software.amazon.awssdk.services.apigateway.model.*;

@ContextConfiguration(classes = {UsagePlanService.class, PnApikeyManagerConfig.class})
@ExtendWith(SpringExtension.class)
@PropertySource("classpath:application-test.properties")
@EnableConfigurationProperties
class UsagePlanServiceTest {

    @MockBean
    private ApiGatewayAsyncClient apiGatewayAsyncClient;

    @Autowired
    private PnApikeyManagerConfig pnApikeyManagerConfig;

    @Autowired
    private UsagePlanService usagePlanService;

    /**
     * Method under test: {@link UsagePlanService#getUsagePlanList()}
     */
    @Test
    void testGetUsagePlanList() {
        Map<String,String> map = new HashMap<>();
        map.put("scope","pn-apikey-manager");
        UsagePlan usagePlan = UsagePlan.builder()
                .tags(map).id("id")
                .throttle(ThrottleSettings.builder().build())
                .quota(QuotaSettings.builder().build())
                .build();
        GetUsagePlansResponse getUsagePlansResponse = GetUsagePlansResponse.builder()
                .items(Collections.singleton(usagePlan)).build();
        CompletableFuture<GetUsagePlansResponse> completableFuture = new CompletableFuture<>();
        completableFuture.completeAsync(() -> getUsagePlansResponse);
        when(apiGatewayAsyncClient.getUsagePlans()).thenReturn(completableFuture);
        UsagePlanResponseDto usagePlanResponseDto = new UsagePlanResponseDto();
        List<UsagePlanDetailDto> usagePlanDetailDtos = new ArrayList<>();
        UsagePlanDetailDto usagePlanDetailDto = new UsagePlanDetailDto();
        usagePlanDetailDto.setId("id");
        usagePlanDetailDtos.add(usagePlanDetailDto);
        usagePlanResponseDto.setItems(usagePlanDetailDtos);
        StepVerifier.create(usagePlanService.getUsagePlanList())
                .expectNext(usagePlanResponseDto).verifyComplete();
    }

    /**
     * Method under test: {@link UsagePlanService#getUsagePlan(String)}
     */
    @Test
    void testGetUsagePlan() {
        GetUsagePlanResponse getUsagePlanResponse = GetUsagePlanResponse.builder()
                .id("id").build();
        CompletableFuture<GetUsagePlanResponse> completableFuture = new CompletableFuture<>();
        completableFuture.completeAsync(() -> getUsagePlanResponse);
        when(apiGatewayAsyncClient.getUsagePlan((GetUsagePlanRequest) any())).thenReturn(completableFuture);
        UsagePlanDetailDto usagePlanDetailDto = new UsagePlanDetailDto();
        usagePlanDetailDto.setId("id");
        StepVerifier.create(usagePlanService.getUsagePlan("id"))
                .expectNext(usagePlanDetailDto).verifyComplete();
    }
}

