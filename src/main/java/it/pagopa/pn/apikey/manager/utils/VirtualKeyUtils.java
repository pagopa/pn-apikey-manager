package it.pagopa.pn.apikey.manager.utils;

import it.pagopa.pn.apikey.manager.constant.RoleConstant;
import it.pagopa.pn.apikey.manager.generated.openapi.server.v1.dto.ApiKeyStatusDto;
import it.pagopa.pn.apikey.manager.generated.openapi.server.v1.dto.RequestVirtualKeyStatusDto;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Set;

public class VirtualKeyUtils {


    public static Mono<Boolean> isRoleAdmin(String xRole, List<String> groups) {
        return Mono.just(RoleConstant.ADMIN_ROLE.equalsIgnoreCase(xRole) && (groups == null || groups.isEmpty()));
    }

    public static String decodeToEntityStatus(RequestVirtualKeyStatusDto.StatusEnum status) {
        return switch (status) {
            case BLOCK -> ApiKeyStatusDto.BLOCKED.name();
            case ENABLE -> ApiKeyStatusDto.ENABLED.name();
            case ROTATE -> ApiKeyStatusDto.ROTATED.name();
        };
    }
}
