package it.pagopa.pn.apikey.manager.converter;

import it.pagopa.pn.apikey.manager.constant.AggregationConstant;
import it.pagopa.pn.apikey.manager.entity.ApiKeyAggregateModel;
import it.pagopa.pn.apikey.manager.generated.openapi.rest.v1.aggregate.dto.AggregateRowDto;
import it.pagopa.pn.apikey.manager.generated.openapi.rest.v1.aggregate.dto.AggregatesListResponseDto;
import it.pagopa.pn.apikey.manager.generated.openapi.rest.v1.aggregate.dto.UsagePlanDetailDto;
import it.pagopa.pn.apikey.manager.generated.openapi.rest.v1.aggregate.dto.*;
import it.pagopa.pn.apikey.manager.entity.PaAggregationModel;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.enhanced.dynamodb.model.Page;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class AggregationConverter {

    public AggregatesListResponseDto convertToResponseDto(@NonNull Page<ApiKeyAggregateModel> page,
                                                          @NonNull List<UsagePlanDetailDto> usagePlans) {
        Map<String, UsagePlanDetailDto> usagePlanMap = usagePlans.stream()
                .collect(Collectors.toMap(UsagePlanDetailDto::getId, Function.identity(), (v1, v2) -> v1));
        AggregatesListResponseDto dto = new AggregatesListResponseDto();
        if (page.lastEvaluatedKey() != null && page.lastEvaluatedKey().containsKey(AggregationConstant.PK)) {
            dto.setLastEvaluatedId(page.lastEvaluatedKey().get(AggregationConstant.PK).s());
            if (page.lastEvaluatedKey().containsKey(AggregationConstant.NAME)) {
                dto.setLastEvaluatedName(page.lastEvaluatedKey().get(AggregationConstant.NAME).s());
            }
        }
        page.items().forEach(item -> dto.addItemsItem(convertToResponseDto(item, usagePlanMap)));
        return dto;
    }

    public AggregateResponseDto convertToResponseDto(@NonNull ApiKeyAggregateModel aggregation,
                                                     @Nullable UsagePlanDetailDto usagePlanDto) {
        AggregateResponseDto dto = new AggregateResponseDto();
        dto.setId(aggregation.getAggregateId());
        dto.setName(aggregation.getName());
        dto.setUsagePlan(usagePlanDto);
        if (aggregation.getCreatedAt() != null) {
            dto.setCreatedAt(Date.from(aggregation.getCreatedAt().toInstant(ZoneOffset.UTC)));
        }
        if (aggregation.getLastUpdate() != null) {
            dto.setLastUpdate(Date.from(aggregation.getLastUpdate().toInstant(ZoneOffset.UTC)));
        }
        return dto;
    }

    public PaAggregateResponseDto convertToResponseDto(@NonNull Page<PaAggregationModel> page) {
        PaAggregateResponseDto dto = new PaAggregateResponseDto();
        dto.setTotal(page.items().size());
        page.items().forEach(item -> dto.addItemsItem(convertToResponseDto(item)));
        return dto;
    }

    public ApiKeyAggregateModel convertToModel(AggregateRequestDto dto) {
        ApiKeyAggregateModel model = new ApiKeyAggregateModel();
        model.setAggregateId(UUID.randomUUID().toString());
        model.setName(dto.getName());
        model.setDescription(dto.getDescription());
        model.setUsagePlanId(dto.getUsagePlanId());
        model.setLastUpdate(LocalDateTime.now());
        model.setCreatedAt(LocalDateTime.now());
        return model;
    }

    private AggregateRowDto convertToResponseDto(ApiKeyAggregateModel aggregation, Map<String, UsagePlanDetailDto> usagePlans) {
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
            dto.setUsagePlan(usagePlans.get(aggregation.getUsagePlanId()).getDescription());
        }
        return dto;
    }

    private PaDetailDto convertToResponseDto(PaAggregationModel paAggregation) {
        PaDetailDto dto = new PaDetailDto();
        dto.setId(paAggregation.getPaId());
        dto.setName(paAggregation.getPaName());
        return dto;
    }
}
