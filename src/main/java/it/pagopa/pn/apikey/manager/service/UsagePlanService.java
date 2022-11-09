package it.pagopa.pn.apikey.manager.service;

import it.pagopa.pn.apikey.manager.config.PnApikeyManagerConfig;
import it.pagopa.pn.apikey.manager.generated.openapi.rest.v1.aggregate.dto.UsagePlanDetailDto;
import it.pagopa.pn.apikey.manager.generated.openapi.rest.v1.aggregate.dto.UsagePlanResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.services.apigateway.ApiGatewayAsyncClient;
import software.amazon.awssdk.services.apigateway.model.GetUsagePlanRequest;
import software.amazon.awssdk.services.apigateway.model.GetUsagePlanResponse;
import software.amazon.awssdk.services.apigateway.model.UsagePlan;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class UsagePlanService {

    private final ApiGatewayAsyncClient apiGatewayAsyncClient;
    private final PnApikeyManagerConfig pnApikeyManagerConfig;

    public UsagePlanService(ApiGatewayAsyncClient apiGatewayAsyncClient,
                            PnApikeyManagerConfig pnApikeyManagerConfig) {
        this.apiGatewayAsyncClient = apiGatewayAsyncClient;
        this.pnApikeyManagerConfig = pnApikeyManagerConfig;
    }

    public Mono<UsagePlanResponseDto> getUsagePlanList() {
        return Mono.fromFuture(apiGatewayAsyncClient.getUsagePlans())
                .map(getUsagePlansResponse -> createUsagePlanResponseDto(getUsagePlansResponse.items()));
    }

    public Mono<UsagePlanDetailDto> getUsagePlan(String usagePlanId) {
        GetUsagePlanRequest usagePlanRequest = GetUsagePlanRequest.builder()
                .usagePlanId(usagePlanId)
                .build();
        return Mono.fromFuture(apiGatewayAsyncClient.getUsagePlan(usagePlanRequest))
                .map(this::convertToUsagePlanDto);
    }

    private UsagePlanResponseDto createUsagePlanResponseDto(List<UsagePlan> items) {
        UsagePlanResponseDto dto = new UsagePlanResponseDto();
        List<UsagePlan> list = items.stream().filter(usagePlan ->
                        usagePlan.tags().get(pnApikeyManagerConfig.getTag()) != null &&
                                usagePlan.tags().get(pnApikeyManagerConfig.getTag()).equalsIgnoreCase(pnApikeyManagerConfig.getScope()))
                .collect(Collectors.toList());
        dto.setItems(convertToUsagePlanDto(list));
        return dto;
    }

    private List<UsagePlanDetailDto> convertToUsagePlanDto(List<UsagePlan> items) {
        return items.stream().map(this::convertToUsagePlanDto).collect(Collectors.toList());
    }

    private UsagePlanDetailDto convertToUsagePlanDto(UsagePlan usagePlan) {
        UsagePlanDetailDto dto = new UsagePlanDetailDto();
        dto.setId(usagePlan.id());
        dto.setName(usagePlan.name());
        if (usagePlan.throttle() != null) {
            dto.setBurst(usagePlan.throttle().burstLimit());
            dto.setRate(usagePlan.throttle().rateLimit());
        }
        if (usagePlan.quota() != null) {
            dto.setQuota(usagePlan.quota().limit());
        }
        return dto;
    }

    private UsagePlanDetailDto convertToUsagePlanDto(GetUsagePlanResponse usagePlan) {
        UsagePlanDetailDto dto = new UsagePlanDetailDto();
        dto.setId(usagePlan.id());
        dto.setName(usagePlan.name());
        if (usagePlan.throttle() != null) {
            dto.setBurst(usagePlan.throttle().burstLimit());
            dto.setRate(usagePlan.throttle().rateLimit());
        }
        if (usagePlan.quota() != null) {
            dto.setQuota(usagePlan.quota().limit());
        }
        return dto;
    }
}