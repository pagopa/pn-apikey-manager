package it.pagopa.pn.apikey.manager.utils;

import it.pagopa.pn.apikey.manager.exception.PnForbiddenException;
import it.pagopa.pn.apikey.manager.generated.openapi.server.v1.dto.CxTypeAuthFleetDto;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

class PublicKeyUtilsTest {

    @Test
    void validaAccessoOnlyAdmin_grantsAccessForAdmin() {

        Mono<Void> result = PublicKeyUtils.validaAccessoOnlyAdmin(CxTypeAuthFleetDto.PG, "ADMIN", List.of());

        StepVerifier.create(result)
                .verifyComplete();
    }

    @Test
    void validaAccessoOnlyAdmin_deniesAccessForNonAdminRole() {

        Mono<Void> result = PublicKeyUtils.validaAccessoOnlyAdmin(CxTypeAuthFleetDto.PG, "OPERATOR", List.of());

        StepVerifier.create(result)
                .expectError(PnForbiddenException.class)
                .verify();
    }

    @Test
    void validaAccessoOnlyAdmin_deniesAccessForNonPGType() {

        Mono<Void> result = PublicKeyUtils.validaAccessoOnlyAdmin(CxTypeAuthFleetDto.PF, "ADMIN", List.of());

        StepVerifier.create(result)
                .expectError(PnForbiddenException.class)
                .verify();
    }

    @Test
    void validaAccessoOnlyAdmin_deniesAccessForNonEmptyGroups() {
        Mono<Void> result = PublicKeyUtils.validaAccessoOnlyAdmin(CxTypeAuthFleetDto.PG, "ADMIN", List.of("group1"));

        StepVerifier.create(result)
                .expectError(PnForbiddenException.class)
                .verify();
    }

    @Test
    void validaAccessoOnlyAdmin_deniesAccessForNullRole() {

        Mono<Void> result = PublicKeyUtils.validaAccessoOnlyAdmin(CxTypeAuthFleetDto.PG, null, List.of());

        StepVerifier.create(result)
                .expectError(PnForbiddenException.class)
                .verify();
    }
}