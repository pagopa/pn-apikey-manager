package it.pagopa.pn.apikey.manager.converter;

import it.pagopa.pn.apikey.manager.constant.AggregationConstant;
import it.pagopa.pn.apikey.manager.entity.ApiKeyAggregateModel;
import it.pagopa.pn.apikey.manager.entity.PaAggregationModel;
import it.pagopa.pn.apikey.manager.generated.openapi.rest.v1.aggregate.dto.*;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.enhanced.dynamodb.model.Page;

import java.time.ZoneOffset;
import java.util.ArrayList;
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

    public AggregateResponseDto convertResponseDto(@NonNull ApiKeyAggregateModel aggregation,
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
            dto.setUsagePlan(usagePlans.get(aggregation.getUsagePlanId()).getName());
        }
        return dto;
    }

    public MovePaResponseDto convertBatchWriteResultToPaResponseDto(int processedSize, int unprocessedSize, List<PaDetailDto> listToUnprocessed, List<PaAggregationModel> listUnprocessed){

        MovePaResponseDto movePaResponseDto = new MovePaResponseDto();
        List<PaDetailDto> unprocessedPA = new ArrayList<>();

        if(listUnprocessed.size()!=0){
            for(PaAggregationModel paAggregationModel : listUnprocessed){
                PaDetailDto paDetailDto = new PaDetailDto();
                paDetailDto.setName(paAggregationModel.getPaName());
                paDetailDto.setId(paAggregationModel.getPaId());
                unprocessedPA.add(paDetailDto);
            }
            unprocessedSize = unprocessedSize + listUnprocessed.size();
        }

        if(listToUnprocessed.size()!=0){
            unprocessedPA.addAll(listToUnprocessed);
        }

        movePaResponseDto.setProcessed(processedSize);
        movePaResponseDto.setUnprocessed(unprocessedSize);
        movePaResponseDto.setUnprocessedPA(unprocessedPA);

        return movePaResponseDto;
    }

}
