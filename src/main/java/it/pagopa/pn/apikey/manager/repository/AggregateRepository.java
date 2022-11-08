package it.pagopa.pn.apikey.manager.repository;

import it.pagopa.pn.apikey.manager.entity.ApiKeyAggregateModel;
import reactor.core.publisher.Mono;

public interface AggregateRepository {

    Mono<ApiKeyAggregateModel> saveAggregation(ApiKeyAggregateModel aggregate);

    Mono<ApiKeyAggregateModel> getApiKeyAggregation(String aggregateId);

    Mono<ApiKeyAggregateModel> delete(String aggregateId);

}
