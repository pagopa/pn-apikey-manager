package it.pagopa.pn.apikey.manager.repository;

import it.pagopa.pn.apikey.manager.entity.ApiKeyAggregateModel;
import reactor.core.publisher.Mono;

public interface AggregateRepository {

    Mono<ApiKeyAggregateModel> saveAggregation(ApiKeyAggregateModel apikeyAggregateModel);

    Mono<ApiKeyAggregateModel> getApiKeyAggregation(String aggregationId);

}
