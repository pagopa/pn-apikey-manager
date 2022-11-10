package it.pagopa.pn.apikey.manager.converter;

import it.pagopa.pn.apikey.manager.constant.AggregationConstant;
import it.pagopa.pn.apikey.manager.entity.ApiKeyAggregateModel;
import it.pagopa.pn.apikey.manager.generated.openapi.rest.v1.aggregate.dto.AggregateResponseDto;
import it.pagopa.pn.apikey.manager.generated.openapi.rest.v1.aggregate.dto.AggregateRowDto;
import it.pagopa.pn.apikey.manager.generated.openapi.rest.v1.aggregate.dto.AggregatesListResponseDto;
import it.pagopa.pn.apikey.manager.generated.openapi.rest.v1.aggregate.dto.UsagePlanDetailDto;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.enhanced.dynamodb.model.Page;

import java.time.ZoneOffset;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class AggregationConverter {

    public AggregatesListResponseDto convertResponseDto(@NonNull Page<ApiKeyAggregateModel> page,
                                                        @NonNull List<UsagePlanDetailDto> usagePlans) {
        Map<String, UsagePlanDetailDto> usagePlanMap = usagePlans.stream()
                .collect(Collectors.toMap(UsagePlanDetailDto::getId, Function.identity(), (v1, v2) -> v1));
        AggregatesListResponseDto dto = new AggregatesListResponseDto();
        if (page.lastEvaluatedKey() != null && page.lastEvaluatedKey().containsKey(AggregationConstant.PK)) {
            dto.setLastEvaluatedId(page.lastEvaluatedKey().get(AggregationConstant.PK).s());
        }
        page.items().forEach(item -> dto.addItemsItem(convertRowDto(item, usagePlanMap)));
        return dto;
    }

    public AggregateResponseDto convertResponseDto(@NonNull ApiKeyAggregateModel aggregation) {
        AggregateResponseDto dto = new AggregateResponseDto();
        // TODO complete converter
        return dto;
    }

    private AggregateRowDto convertRowDto(ApiKeyAggregateModel aggregation, Map<String, UsagePlanDetailDto> usagePlans) {
        AggregateRowDto dto = new AggregateRowDto();
        dto.setId(aggregation.getAggregateId());
        dto.setName(aggregation.getName());
        if (aggregation.getCreatedAt() != null) {
            dto.setCreatedAt(Date.from(aggregation.getCreatedAt().toInstant(ZoneOffset.UTC)));
        }
        if (aggregation.getLastUpdate() != null) {
            dto.setLastUpdate(Date.from(aggregation.getLastUpdate().toInstant(ZoneOffset.UTC)));
        }
        if (aggregation.getUsagePlanId() != null && usagePlans.containsKey(aggregation.getUsagePlanId())) {
            dto.setUsagePlanTemplate(usagePlans.get(aggregation.getUsagePlanId()).getName());
        }
        return dto;
    }

}
