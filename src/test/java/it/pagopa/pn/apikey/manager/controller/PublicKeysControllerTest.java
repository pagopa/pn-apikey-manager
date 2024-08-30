package it.pagopa.pn.apikey.manager.controller;

import it.pagopa.pn.apikey.manager.generated.openapi.server.v1.dto.CxTypeAuthFleetDto;
import it.pagopa.pn.apikey.manager.generated.openapi.server.v1.dto.PublicKeyRowDto;
import it.pagopa.pn.apikey.manager.generated.openapi.server.v1.dto.PublicKeysResponseDto;
import it.pagopa.pn.apikey.manager.service.PublicKeyService;
import it.pagopa.pn.commons.log.PnAuditLogBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
class PublicKeysControllerTest {

    private PublicKeyService publicKeyService;
    private PublicKeysController publicKeysController;

    @BeforeEach
    void setUp() {
        publicKeyService = mock(PublicKeyService.class);
        publicKeysController = new PublicKeysController(publicKeyService, new PnAuditLogBuilder());
    }

    @Test
    void testGetPublicKeys() {
        String xPagopaPnUid = "uidTest";
        CxTypeAuthFleetDto xPagopaPnCxType = CxTypeAuthFleetDto.PG;
        String xPagopaPnCxId = "user1";
        List<String> xPagopaPnCxGroups = new ArrayList<>();
        xPagopaPnCxGroups.add("RECLAMI");
        String xPagopaPnCxRole = "ADMIN";
        Boolean showPublicKey = true;
        String lastKey = "72a081da-4bd3-11ed-bdc3-0242ac120002";
        String createdAt = "2024-10-25T16:25:58.334862500";

        PublicKeysResponseDto publicKeysResponseDto = new PublicKeysResponseDto();
        List<PublicKeyRowDto> publicKeyRowDtos = new ArrayList<>();
        publicKeysResponseDto.setItems(publicKeyRowDtos);
        publicKeysResponseDto.setLastKey(lastKey);
        publicKeysResponseDto.setCreatedAt(createdAt);

        when(publicKeyService.getPublicKeys(eq(xPagopaPnCxType), anyString(), anyList(), anyString(), anyInt(), anyString(), anyString(), anyBoolean()))
                .thenReturn(Mono.just(publicKeysResponseDto));
        StepVerifier.create(publicKeysController.getPublicKeys(xPagopaPnUid, xPagopaPnCxType, xPagopaPnCxId, xPagopaPnCxGroups, xPagopaPnCxRole, 10, lastKey, createdAt, showPublicKey, null))
                .expectNext(ResponseEntity.ok().body(publicKeysResponseDto));
    }
}