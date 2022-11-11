package it.pagopa.pn.apikey.manager.repository;

import it.pagopa.pn.apikey.manager.entity.PaAggregationModel;
import it.pagopa.pn.apikey.manager.generated.openapi.rest.v1.aggregate.dto.AddPaListRequestDto;
import it.pagopa.pn.apikey.manager.generated.openapi.rest.v1.aggregate.dto.PaDetailDto;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.enhanced.dynamodb.model.BatchGetResultPage;
import software.amazon.awssdk.enhanced.dynamodb.model.BatchWriteResult;
import software.amazon.awssdk.enhanced.dynamodb.model.Page;

import java.util.List;

public interface PaAggregationRepository {

    Mono<PaAggregationModel> searchAggregation(String xPagopaPnCxId);

    Mono<PaAggregationModel> savePaAggregation(PaAggregationModel toSave);

    Flux<BatchWriteResult> savePaAggregation(List<PaAggregationModel> toSave);

    Mono<Page<PaAggregationModel>> getAllPaAggregations();

    Mono<Page<PaAggregationModel>> findByAggregateId(String aggregateId, Integer limit, String lastKey);

    Flux<BatchGetResultPage>  batchGetItem(AddPaListRequestDto addPaListRequestDto);
}
