package it.pagopa.pn.apikey.manager.service;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import it.pagopa.pn.apikey.manager.config.PnApikeyManagerConfig;

import java.util.concurrent.CompletableFuture;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import software.amazon.awssdk.services.apigateway.ApiGatewayAsyncClient;
import software.amazon.awssdk.services.apigateway.model.GetUsagePlanRequest;

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
        when(apiGatewayAsyncClient.getUsagePlans()).thenReturn(new CompletableFuture<>());
        usagePlanService.getUsagePlanList();
        verify(apiGatewayAsyncClient).getUsagePlans();
    }

    /**
     * Method under test: {@link UsagePlanService#getUsagePlan(String)}
     */
    @Test
    void testGetUsagePlan() {
        when(apiGatewayAsyncClient.getUsagePlan((GetUsagePlanRequest) any())).thenReturn(new CompletableFuture<>());
        usagePlanService.getUsagePlan("42");
        verify(apiGatewayAsyncClient).getUsagePlan((GetUsagePlanRequest) any());
    }
}

