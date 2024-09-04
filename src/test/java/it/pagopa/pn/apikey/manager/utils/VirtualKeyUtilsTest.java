package it.pagopa.pn.apikey.manager.utils;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Collections;
import java.util.List;

class VirtualKeyUtilsTest {

    @Test
    void isRoleAdmin_withAdminRoleAndEmptyGroups_returnsTrue() {
        Mono<Boolean> result = VirtualKeyUtils.isRoleAdmin("ADMIN", Collections.emptyList());

        StepVerifier.create(result)
                .expectNext(true)
                .verifyComplete();
    }

    @Test
    void isRoleAdmin_withAdminRoleAndNonEmptyGroups_returnsFalse() {
        Mono<Boolean> result = VirtualKeyUtils.isRoleAdmin("ADMIN", List.of("group1"));

        StepVerifier.create(result)
                .expectNext(false)
                .verifyComplete();
    }

    @Test
    void isRoleAdmin_withNonAdminRoleAndEmptyGroups_returnsFalse() {
        Mono<Boolean> result = VirtualKeyUtils.isRoleAdmin("USER", Collections.emptyList());

        StepVerifier.create(result)
                .expectNext(false)
                .verifyComplete();
    }

    @Test
    void isRoleAdmin_withNonAdminRoleAndNonEmptyGroups_returnsFalse() {
        Mono<Boolean> result = VirtualKeyUtils.isRoleAdmin("USER", List.of("group1"));

        StepVerifier.create(result)
                .expectNext(false)
                .verifyComplete();
    }

    @Test
    void isRoleAdmin_withNullRoleAndEmptyGroups_returnsFalse() {
        Mono<Boolean> result = VirtualKeyUtils.isRoleAdmin(null, Collections.emptyList());

        StepVerifier.create(result)
                .expectNext(false)
                .verifyComplete();
    }

    @Test
    void isRoleAdmin_withAdminRoleAndNullGroups_returnsFalse() {
        Mono<Boolean> result = VirtualKeyUtils.isRoleAdmin("ADMIN", null);

        StepVerifier.create(result)
                .expectNext(true)
                .verifyComplete();
    }
}