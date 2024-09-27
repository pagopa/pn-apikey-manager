package it.pagopa.pn.apikey.manager.utils;

import it.pagopa.pn.apikey.manager.generated.openapi.server.v1.dto.ApiKeyStatusDto;
import it.pagopa.pn.apikey.manager.generated.openapi.server.v1.dto.RequestVirtualKeyStatusDto;
import reactor.core.publisher.Mono;

import java.util.List;

public class VirtualKeyUtils {

    public static Mono<Boolean> isRoleAdmin(String xRole, List<String> groups) {
        return Mono.just("ADMIN".equals(xRole) && (groups == null || groups.isEmpty()));
    }

    public static String decodeToEntityStatus(RequestVirtualKeyStatusDto.StatusEnum status) {
        return switch (status) {
            case BLOCK -> ApiKeyStatusDto.BLOCKED.name();
            case ENABLE -> ApiKeyStatusDto.ENABLED.name();
            case ROTATE -> ApiKeyStatusDto.ROTATED.name();
        };
    }
}
