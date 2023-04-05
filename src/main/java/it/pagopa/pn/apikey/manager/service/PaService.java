package it.pagopa.pn.apikey.manager.service;

import it.pagopa.pn.apikey.manager.client.ExternalRegistriesClient;
import it.pagopa.pn.apikey.manager.entity.PaAggregationModel;
import it.pagopa.pn.apikey.manager.exception.ApiKeyManagerException;
import it.pagopa.pn.apikey.manager.generated.openapi.rest.v1.aggregate.dto.*;
import it.pagopa.pn.apikey.manager.model.PnBatchGetItemResponse;
import it.pagopa.pn.apikey.manager.model.PnBatchPutItemResponse;
import it.pagopa.pn.apikey.manager.repository.AggregateRepository;
import it.pagopa.pn.apikey.manager.repository.PaAggregationPageable;
import it.pagopa.pn.apikey.manager.repository.PaAggregationRepository;
import it.pagopa.pn.apikey.manager.utils.DynamoBatchResponseUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.enhanced.dynamodb.Key;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static it.pagopa.pn.apikey.manager.exception.ApiKeyManagerExceptionError.AGGREGATE_NOT_FOUND;

@Slf4j
@Service
public class PaService {

    private final AggregateRepository aggregateRepository;
    private final PaAggregationRepository paAggregationRepository;
    private final ExternalRegistriesClient externalRegistriesClient;
    private final DynamoBatchResponseUtils dynamoBatchResponseUtils;

    public PaService(AggregateRepository aggregateRepository,
                     PaAggregationRepository paAggregationRepository,
                     ExternalRegistriesClient externalRegistriesClient,
                     DynamoBatchResponseUtils dynamoBatchResponseUtils) {
        this.dynamoBatchResponseUtils = dynamoBatchResponseUtils;
        this.aggregateRepository = aggregateRepository;
        this.paAggregationRepository = paAggregationRepository;
        this.externalRegistriesClient = externalRegistriesClient;
    }

    public Mono<GetPaResponseDto> getPaList(@Nullable Integer limit,
                                            @Nullable String lastEvaluatedKey){
        PaAggregationPageable pageable = toPaPageable(limit, lastEvaluatedKey);
        return paAggregationRepository.getAll(pageable)
                .map(paAggregationModels -> {
                    GetPaResponseDto dto = new GetPaResponseDto();
                    dto.setItems(paAggregationModels.items().stream().map(paAggregationModel -> {
                        PaDetailDto paDetailDto = new PaDetailDto();
                        paDetailDto.setName(paAggregationModel.getPaName());
                        paDetailDto.setId(paAggregationModel.getPaId());
                        return paDetailDto;
                    }).toList());
                    return dto;
                });
    }

    public Mono<AssociablePaResponseDto> getAssociablePa(String name) {
        return externalRegistriesClient.getAllPa(name)
                .doOnError(e -> log.warn("can not get PA from external registries with name {}", name, e))
                .doOnNext(paDetailDtos -> log.info("list of onboarded Pa size: {}", paDetailDtos.size()))
                .flatMap(this::filterForResponse);
    }

    private Mono<AssociablePaResponseDto> filterForResponse(List<PaDetailDto> list) {
        return paAggregationRepository.getAllPaAggregations()
                .map(paAggregationModels -> {
                    AssociablePaResponseDto dto = new AssociablePaResponseDto();
                    dto.setItems(list.stream().filter(paDetailDto ->
                            paAggregationModels.items().stream().noneMatch(paAggregationModel ->
                                    paAggregationModel.getPaId().equalsIgnoreCase(paDetailDto.getId())
                            )).collect(Collectors.toList()));
                    return dto;
                });
    }

    public Mono<MovePaResponseDto> createNewPaAggregation(String aggregateId, AddPaListRequestDto addPaListRequestDto) {
        log.debug("creating PaAggregation for aggregate {}", aggregateId);
        addPaListRequestDto.setItems(addPaListRequestDto.getItems().stream()
                .distinct()
                .filter(paDetailDto -> StringUtils.hasText(paDetailDto.getId()))
                .collect(Collectors.toList()));
        MovePaResponseDto movePaResponseDto = new MovePaResponseDto();
        movePaResponseDto.setUnprocessedPA(new ArrayList<>());
        return savePaAggregation(aggregateId, createPaAggregationModel(aggregateId, addPaListRequestDto.getItems()), movePaResponseDto);
    }


    public Mono<MovePaResponseDto> movePa(String id, MovePaListRequestDto movePaListRequestDto) {
        AddPaListRequestDto addPaListRequestDto = new AddPaListRequestDto();
        List<PaDetailDto> paDetailDtos = movePaListRequestDto
                .getItems()
                .stream().map(paMoveDetailDto -> {
                    PaDetailDto paDetailDto = new PaDetailDto();
                    paDetailDto.setId(paMoveDetailDto.getId());
                    return paDetailDto;
                }).toList();
        addPaListRequestDto.setItems(paDetailDtos);
        log.debug("start movePa for {} PA to aggregate: {}", addPaListRequestDto.getItems().size(), id);
        addPaListRequestDto.setItems(addPaListRequestDto.getItems().stream()
                .distinct()
                .filter(paDetailDto -> StringUtils.hasText(paDetailDto.getId()))
                .collect(Collectors.toList()));
        return paAggregationRepository.batchGetItem(addPaListRequestDto).collectList()
                .doOnNext(batchGetResultPages -> log.info("BatchGetResultPage size: {}", batchGetResultPages.size()))
                .flatMap(batchGetResultPage -> {
                    MovePaResponseDto movePaResponseDto = new MovePaResponseDto();
                    movePaResponseDto.setUnprocessedPA(new ArrayList<>());
                    ArrayList<PaAggregationModel> paAggregationModelsToSave = new ArrayList<>();
                    batchGetResultPage.forEach(result -> {
                        PnBatchGetItemResponse pnBatchGetItemResponse = dynamoBatchResponseUtils.convertPaAggregationsBatchGetItemResponse(result);
                        paAggregationModelsToSave.addAll(pnBatchGetItemResponse.getFounded()
                                .stream().map(paAggregationModel -> {
                                    paAggregationModel.setAggregateId(id);
                                    return paAggregationModel;
                                }).toList());
                        countAndConvertUnprocessedGetItem(movePaResponseDto, pnBatchGetItemResponse, addPaListRequestDto.getItems());
                    });
                    addPaListRequestDto.getItems().removeIf(paDetailDto -> movePaResponseDto.getUnprocessedPA().contains(paDetailDto));
                    return savePaAggregation(id, paAggregationModelsToSave, movePaResponseDto);
                });
    }

    private Mono<MovePaResponseDto> savePaAggregation(String aggregateId, List<PaAggregationModel> items, MovePaResponseDto movePaResponseDto) {
        return aggregateRepository.getApiKeyAggregation(aggregateId)
                .switchIfEmpty(Mono.error(new ApiKeyManagerException(AGGREGATE_NOT_FOUND, HttpStatus.NOT_FOUND)))
                .flatMap(aggregate -> paAggregationRepository.savePaAggregation(items).collectList())
                .doOnNext(batchWriteResults -> log.info("BatchWriteResult size: {}", batchWriteResults.size()))
                .map(batchWriteResult -> {
                    batchWriteResult.forEach(result -> {
                        PnBatchPutItemResponse pnBatchPutItemResponse = dynamoBatchResponseUtils.convertPaAggregationsBatchPutItemResponse(result);
                        countAndConvertUnprocessedPutItem(movePaResponseDto, pnBatchPutItemResponse, items);
                    });
                    return movePaResponseDto;
                })
                .switchIfEmpty(Mono.just(movePaResponseDto));
    }

    private void countAndConvertUnprocessedPutItem(MovePaResponseDto movePaResponseDto, PnBatchPutItemResponse pnBatchPutItemResponse, List<PaAggregationModel> items) {
        movePaResponseDto.getUnprocessedPA()
                .addAll(convertUnprocessedModel(pnBatchPutItemResponse.getUnprocessed(), movePaResponseDto.getUnprocessedPA()));
        movePaResponseDto.setUnprocessed(movePaResponseDto.getUnprocessed() + pnBatchPutItemResponse.getUnprocessed().size());
        movePaResponseDto.setProcessed(items.size() - pnBatchPutItemResponse.getUnprocessed().size());
        log.info("MovePaResponseDto after countAndConvertUnprocessedPutItem: {}", movePaResponseDto);
    }

    private void countAndConvertUnprocessedGetItem(MovePaResponseDto movePaResponseDto, PnBatchGetItemResponse pnBatchGetItemResponse, List<PaDetailDto> items) {
        List<PaDetailDto> unprocessedList = items.stream()
                .filter(paDetailDto -> pnBatchGetItemResponse.getFounded().stream().noneMatch(paAggregationModel -> paDetailDto.getId().equalsIgnoreCase(paAggregationModel.getPaId())))
                .collect(Collectors.toList());
        movePaResponseDto.getUnprocessedPA().addAll(unprocessedList);
        movePaResponseDto.getUnprocessedPA().addAll(convertUnprocessedKey(pnBatchGetItemResponse.getUnprocessed(), items));
        movePaResponseDto.setUnprocessed(pnBatchGetItemResponse.getUnprocessed().size() + unprocessedList.size());
        log.info("MovePaResponseDto after countAndConvertUnprocessedGetItem: {}", movePaResponseDto);
    }

    private List<PaDetailDto> convertUnprocessedKey(List<Key> unprocessedKeysForTable, List<PaDetailDto> list) {
        return list.stream().filter(paDetailDto ->
                unprocessedKeysForTable.stream().anyMatch(key ->
                        paDetailDto.getId().equalsIgnoreCase(key.partitionKeyValue().s()))).collect(Collectors.toList());
    }

    private List<PaDetailDto> convertUnprocessedModel(List<PaAggregationModel> unprocessedKeysForTable, List<PaDetailDto> list) {
        return list.stream().filter(paDetailDto ->
                unprocessedKeysForTable.stream().anyMatch(key ->
                        paDetailDto.getId().equalsIgnoreCase(key.getPaId()))).collect(Collectors.toList());
    }

    private List<PaAggregationModel> createPaAggregationModel(String id, List<PaDetailDto> items) {
        List<PaAggregationModel> list = new ArrayList<>();
        for (PaDetailDto detailDto : items) {
            PaAggregationModel paAggregationModel = new PaAggregationModel();
            paAggregationModel.setAggregateId(id);
            paAggregationModel.setPaId(detailDto.getId());
            paAggregationModel.setPaName(detailDto.getName());
            list.add(paAggregationModel);
        }
        return list;
    }

    private PaAggregationPageable toPaPageable(Integer limit, String lastEvaluatedLastKey) {
        return PaAggregationPageable.builder()
                .limit(limit)
                .lastEvaluatedKey(lastEvaluatedLastKey)
                .build();
    }
}
