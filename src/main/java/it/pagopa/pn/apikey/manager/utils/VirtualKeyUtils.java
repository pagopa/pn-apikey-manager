package it.pagopa.pn.apikey.manager.utils;

import reactor.core.publisher.Mono;

import java.util.List;

public class VirtualKeyUtils {

    public static Mono<Boolean> isRoleAdmin(String xRole, List<String> groups) {
        return Mono.just("ADMIN".equals(xRole) && (groups == null || groups.isEmpty()));
    }
}
