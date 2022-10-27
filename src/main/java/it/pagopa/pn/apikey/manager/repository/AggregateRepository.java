package it.pagopa.pn.apikey.manager.repository;

import it.pagopa.pn.apikey.manager.entity.ApiKeyAggregation;
import reactor.core.publisher.Mono;

public interface AggregateRepository {

    Mono<ApiKeyAggregation> saveAggregation(ApiKeyAggregation apiKeyAggregation);

    Mono<ApiKeyAggregation> getApiKeyAggregation(String aggregationId);

}
