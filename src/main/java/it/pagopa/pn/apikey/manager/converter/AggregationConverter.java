package it.pagopa.pn.apikey.manager.converter;

import it.pagopa.pn.apikey.manager.entity.ApiKeyAggregateModel;
import it.pagopa.pn.apikey.manager.generated.openapi.rest.v1.aggregate.dto.AggregateResponseDto;
import it.pagopa.pn.apikey.manager.generated.openapi.rest.v1.aggregate.dto.AggregateRowDto;
import it.pagopa.pn.apikey.manager.generated.openapi.rest.v1.aggregate.dto.AggregatesListResponseDto;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.enhanced.dynamodb.model.Page;

@Component
public class AggregationConverter {

    public AggregatesListResponseDto convertResponseDto(Page<ApiKeyAggregateModel> page) {
        AggregatesListResponseDto dto = new AggregatesListResponseDto();
        // TODO complete converter
        return dto;
    }

    public AggregateResponseDto convertResponseDto(ApiKeyAggregateModel aggregation) {
        AggregateResponseDto dto = new AggregateResponseDto();
        // TODO complete converter
        return dto;
    }

    private AggregateRowDto convertRowDto(ApiKeyAggregateModel aggregation) {
        AggregateRowDto dto = new AggregateRowDto();
        // TODO complete converter
        return dto;
    }

}
