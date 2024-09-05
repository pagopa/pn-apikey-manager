package it.pagopa.pn.apikey.manager.controller;

import it.pagopa.pn.apikey.manager.generated.openapi.server.v1.dto.CxTypeAuthFleetDto;
import it.pagopa.pn.apikey.manager.generated.openapi.server.v1.dto.VirtualKeyDto;
import it.pagopa.pn.apikey.manager.generated.openapi.server.v1.dto.VirtualKeysResponseDto;
import it.pagopa.pn.apikey.manager.service.VirtualKeyService;
import it.pagopa.pn.commons.log.PnAuditLogBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
class VirtualKeyControllerTest {

    private VirtualKeyService virtualKeyService;
    private VirtualKeyController virtualKeyController;
    @MockBean
    ServerWebExchange serverWebExchange;

    @BeforeEach
    void setUp() {
        virtualKeyService = mock(VirtualKeyService.class);
        virtualKeyController = new VirtualKeyController(virtualKeyService, new PnAuditLogBuilder());
    }

    @Test
    void testGetVirtualKeys() {
        String xPagopaPnUid = "uid";
        CxTypeAuthFleetDto xPagopaPnCxType = CxTypeAuthFleetDto.PG;
        String xPagopaPnCxId = "user1";
        List<String> xPagopaPnCxGroups = new ArrayList<>();
        xPagopaPnCxGroups.add("RECLAMI");
        Boolean showVirtualKey = true;
        String lastKey = "72a081da-4bd3-11ed-bdc3-0242ac120002";
        String lastUpdate = "2022-10-25T16:25:58.334862500";

        VirtualKeysResponseDto virtualKeysResponseDto = new VirtualKeysResponseDto();
        List<VirtualKeyDto> virtualKeyDtos = new ArrayList<>();
        virtualKeysResponseDto.setItems(virtualKeyDtos);
        virtualKeysResponseDto.setLastKey(lastKey);
        virtualKeysResponseDto.setLastUpdate(lastUpdate);

        when(virtualKeyService.getVirtualKeys(xPagopaPnUid, xPagopaPnCxType, xPagopaPnCxId, xPagopaPnCxGroups, "ADMIN", 10, lastKey, lastUpdate, showVirtualKey))
                .thenReturn(Mono.just(virtualKeysResponseDto));

        StepVerifier.create(virtualKeyController.getVirtualKeys(xPagopaPnUid, xPagopaPnCxType, xPagopaPnCxId, "ADMIN", xPagopaPnCxGroups, 10, lastKey, lastUpdate, showVirtualKey, serverWebExchange))
                .expectNext(ResponseEntity.ok().body(virtualKeysResponseDto))
                .verifyComplete();
    }

}
