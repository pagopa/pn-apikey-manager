package it.pagopa.pn.apikey.manager.repository;

import it.pagopa.pn.apikey.manager.entity.ApiKeyAggregateModel;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.enhanced.dynamodb.model.Page;

public interface AggregateRepository {

    Mono<Page<ApiKeyAggregateModel>> findAll(AggregatePageable pageable);

    Mono<Integer> count();

    Mono<Page<ApiKeyAggregateModel>> findByName(String name, AggregatePageable pageable);

    Mono<Integer> countByName(String name);

    Mono<ApiKeyAggregateModel> saveAggregation(ApiKeyAggregateModel aggregate);

    Mono<ApiKeyAggregateModel> getApiKeyAggregation(String aggregateId);

    Mono<ApiKeyAggregateModel> delete(String aggregateId);

}
