package it.pagopa.pn.apikey.manager.utils;

import it.pagopa.pn.apikey.manager.constant.RoleConstant;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Collections;
import java.util.List;

class VirtualKeyUtilsTest {

    @Test
    void isRoleAdmin_withAdminRoleAndEmptyGroups_returnsTrue() {
        Mono<Boolean> result = VirtualKeyUtils.isRoleAdmin(RoleConstant.ADMIN_ROLE, Collections.emptyList());

        StepVerifier.create(result)
                .expectNext(true)
                .verifyComplete();
    }

    @Test
    void isRoleAdmin_withAdminRoleAndNonEmptyGroups_returnsFalse() {
        Mono<Boolean> result = VirtualKeyUtils.isRoleAdmin(RoleConstant.ADMIN_ROLE, List.of("group1"));

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
        Mono<Boolean> result = VirtualKeyUtils.isRoleAdmin(RoleConstant.ADMIN_ROLE, null);

        StepVerifier.create(result)
                .expectNext(true)
                .verifyComplete();
    }
}