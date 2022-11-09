package it.pagopa.pn.apikey.manager.service;

import it.pagopa.pn.apikey.manager.config.PnApikeyManagerConfig;
import it.pagopa.pn.apikey.manager.generated.openapi.rest.v1.aggregate.dto.UsagePlanDetailDto;
import it.pagopa.pn.apikey.manager.generated.openapi.rest.v1.aggregate.dto.UsagePlanResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.services.apigateway.ApiGatewayAsyncClient;
import software.amazon.awssdk.services.apigateway.model.UsagePlan;

import java.util.ArrayList;
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

    private UsagePlanResponseDto createUsagePlanResponseDto(List<UsagePlan> items) {
        UsagePlanResponseDto dto = new UsagePlanResponseDto();
        List<UsagePlan> list = items.stream().filter(usagePlan ->
                        usagePlan.tags().get(pnApikeyManagerConfig.getTag()) != null &&
                                usagePlan.tags().get(pnApikeyManagerConfig.getTag()).equalsIgnoreCase(pnApikeyManagerConfig.getScope()))
                .collect(Collectors.toList());
        dto.setItems(convertToUsagePlanTemplate(list));
        return dto;
    }

    private List<UsagePlanDetailDto> convertToUsagePlanTemplate(List<UsagePlan> items) {
        List<UsagePlanDetailDto> list = new ArrayList<>();
        for (UsagePlan usagePlan : items) {
            UsagePlanDetailDto dto = new UsagePlanDetailDto();
            dto.setId(usagePlan.id());
            dto.setName(usagePlan.name());
            if(usagePlan.throttle()!=null) {
                dto.setBurst(usagePlan.throttle().burstLimit());
                dto.setRate(usagePlan.throttle().rateLimit());
            }
            if(usagePlan.quota()!=null) {
                dto.setQuota(usagePlan.quota().limit());
            }
            list.add(dto);
        }
        return list;
    }
}
