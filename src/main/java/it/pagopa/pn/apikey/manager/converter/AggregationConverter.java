package it.pagopa.pn.apikey.manager.converter;

import it.pagopa.pn.apikey.manager.entity.ApiKeyAggregation;
import it.pagopa.pn.apikey.manager.generated.openapi.rest.v1.dto.AggregateResponseDto;
import it.pagopa.pn.apikey.manager.generated.openapi.rest.v1.dto.AggregateRowDto;
import it.pagopa.pn.apikey.manager.generated.openapi.rest.v1.dto.AggregatesListResponseDto;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.enhanced.dynamodb.model.Page;

@Component
public class AggregationConverter {

    public AggregatesListResponseDto convertResponseDto(Page<ApiKeyAggregation> page) {
        AggregatesListResponseDto dto = new AggregatesListResponseDto();
        // TODO complete converter
        return dto;
    }

    public AggregateResponseDto convertResponseDto(ApiKeyAggregation aggregation) {
        AggregateResponseDto dto = new AggregateResponseDto();
        // TODO complete converter
        return dto;
    }

    private AggregateRowDto convertRowDto(ApiKeyAggregation aggregation) {
        AggregateRowDto dto = new AggregateRowDto();
        // TODO complete converter
        return dto;
    }

}
