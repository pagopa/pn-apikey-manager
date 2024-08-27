package it.pagopa.pn.apikey.manager.repository;

import it.pagopa.pn.apikey.manager.entity.AuthJwtIssuerModel;
import reactor.core.publisher.Mono;

public interface AuthJwtIssuerRepository {

    Mono<AuthJwtIssuerModel> save(AuthJwtIssuerModel authJwtIssuerModel);

}
