package it.pagopa.pn.apikey.manager.converter;

import it.pagopa.pn.apikey.manager.constant.AggregationConstant;
import it.pagopa.pn.apikey.manager.entity.ApiKeyAggregateModel;
import it.pagopa.pn.apikey.manager.generated.openapi.rest.v1.aggregate.dto.AggregateResponseDto;
import it.pagopa.pn.apikey.manager.generated.openapi.rest.v1.aggregate.dto.AggregateRowDto;
import it.pagopa.pn.apikey.manager.generated.openapi.rest.v1.aggregate.dto.AggregatesListResponseDto;
import it.pagopa.pn.apikey.manager.generated.openapi.rest.v1.aggregate.dto.UsagePlanDetailDto;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.enhanced.dynamodb.model.Page;

import java.time.ZoneOffset;
import java.util.Date;
import java.util.Map;

@Component
public class AggregationConverter {

    public AggregatesListResponseDto convertResponseDto(Page<ApiKeyAggregateModel> page, Map<String, UsagePlanDetailDto> usagePlans) {
        AggregatesListResponseDto dto = new AggregatesListResponseDto();
        if (page.lastEvaluatedKey() != null) {
            if (page.lastEvaluatedKey().containsKey(AggregationConstant.PK)) {
                dto.setLastEvaluatedId(page.lastEvaluatedKey().get(AggregationConstant.PK).s());
            }
        }
        page.items().forEach(item -> dto.addItemsItem(convertRowDto(item, usagePlans)));
        return dto;
    }

    public AggregateResponseDto convertResponseDto(ApiKeyAggregateModel aggregation) {
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
