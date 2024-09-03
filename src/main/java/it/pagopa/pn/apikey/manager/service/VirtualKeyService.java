package it.pagopa.pn.apikey.manager.service;

import it.pagopa.pn.apikey.manager.exception.ApiKeyManagerException;
import it.pagopa.pn.apikey.manager.generated.openapi.server.v1.dto.CxTypeAuthFleetDto;
import it.pagopa.pn.apikey.manager.generated.openapi.server.v1.dto.RequestVirtualKeyStatusDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@lombok.CustomLog
@Slf4j
@RequiredArgsConstructor
public class VirtualKeyService {
    public Mono<Void> changeStatusVirtualKeys(String xPagopaPnUid, CxTypeAuthFleetDto xPagopaPnCxType, String xPagopaPnCxId, String xPagopaPnCxRole, String id, RequestVirtualKeyStatusDto requestVirtualKeyStatusDto) {
        log.info("Starting changeStatusVirtualKeys - id={}, xPagopaPnUid={}", id, xPagopaPnUid);
        return switch (requestVirtualKeyStatusDto.getStatus()) {
            case ENABLE, BLOCK ->
                    reactivateOrBlockVirtualKey(id, xPagopaPnUid, xPagopaPnCxType, xPagopaPnCxId, xPagopaPnCxRole, requestVirtualKeyStatusDto);
            case ROTATE ->
                    rotateVirtualKey(id, xPagopaPnUid, xPagopaPnCxType, xPagopaPnCxId, xPagopaPnCxRole, requestVirtualKeyStatusDto);
            default -> {
                log.error("Invalid VirtualKey status - id={}, status={}", id, requestVirtualKeyStatusDto.getStatus());
                yield Mono.error(new ApiKeyManagerException("Invalid VirtualKey status", HttpStatus.BAD_REQUEST));
            }
        };
    }

    private Mono<Void> rotateVirtualKey(String id, String xPagopaPnUid, CxTypeAuthFleetDto xPagopaPnCxType, String xPagopaPnCxId, String xPagopaPnCxRole, RequestVirtualKeyStatusDto requestVirtualKeyStatusDto) {
        return null;
    }

    private Mono<Void> reactivateOrBlockVirtualKey(String id, String xPagopaPnUid, CxTypeAuthFleetDto xPagopaPnCxType, String xPagopaPnCxId, String xPagopaPnCxRole, RequestVirtualKeyStatusDto requestVirtualKeyStatusDto) {
        return null;
    }
}
