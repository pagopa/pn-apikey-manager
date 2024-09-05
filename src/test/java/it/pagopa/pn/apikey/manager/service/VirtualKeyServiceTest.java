package it.pagopa.pn.apikey.manager.service;

import it.pagopa.pn.apikey.manager.entity.ApiKeyModel;
import it.pagopa.pn.apikey.manager.generated.openapi.server.v1.dto.CxTypeAuthFleetDto;
import it.pagopa.pn.apikey.manager.generated.openapi.server.v1.dto.RequestVirtualKeyStatusDto;
import it.pagopa.pn.apikey.manager.repository.ApiKeyRepository;
import it.pagopa.pn.apikey.manager.validator.VirtualKeyValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class VirtualKeyServiceTest {

    @Mock
    private ApiKeyRepository apiKeyRepository;

    @Mock
    private VirtualKeyValidator virtualKeyValidator;

    @InjectMocks
    private VirtualKeyService virtualKeyService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testChangeStatusVirtualKeys_Enable() {

        String id = "keyId";
        String xPagopaPnUid = "userUid";
        String xPagopaPnCxId = "cxId";
        String xPagopaPnCxRole = "admin";
        List<String> xPagopaPnCxGroups = List.of("group1", "group2");

        RequestVirtualKeyStatusDto requestDto = new RequestVirtualKeyStatusDto();
        requestDto.setStatus(RequestVirtualKeyStatusDto.StatusEnum.ENABLE);

        when(apiKeyRepository.findById(id)).thenReturn(Mono.just(new ApiKeyModel()));
        when(virtualKeyValidator.checkCxIdAndUid(anyString(), anyString(), any())).thenReturn(Mono.just(new ApiKeyModel()));
        when(virtualKeyValidator.validateStateTransition(any(), any())).thenReturn(Mono.empty());
        when(virtualKeyValidator.validateNoOtherKeyWithSameStatus(anyString(), anyString(), anyString())).thenReturn(Mono.empty());
        when(apiKeyRepository.save(any(ApiKeyModel.class))).thenReturn(Mono.just(new ApiKeyModel()));

        Mono<Void> result = virtualKeyService.changeStatusVirtualKeys(xPagopaPnUid, CxTypeAuthFleetDto.PG, xPagopaPnCxId, xPagopaPnCxRole, id, requestDto, xPagopaPnCxGroups);

        StepVerifier.create(result)
                .verifyComplete();

        verify(apiKeyRepository, times(1)).findById(id);
        verify(apiKeyRepository, times(1)).save(any(ApiKeyModel.class));
        verify(virtualKeyValidator, times(1)).validateNoOtherKeyWithSameStatus(anyString(), anyString(), anyString());
    }

    @Test
    void testChangeStatusVirtualKeys_Block() {

        String id = "keyId";
        String xPagopaPnUid = "userUid";
        String xPagopaPnCxId = "cxId";
        String xPagopaPnCxRole = "admin";
        List<String> xPagopaPnCxGroups = List.of("group1", "group2");

        RequestVirtualKeyStatusDto requestDto = new RequestVirtualKeyStatusDto();
        requestDto.setStatus(RequestVirtualKeyStatusDto.StatusEnum.BLOCK);

        when(apiKeyRepository.findById(id)).thenReturn(Mono.just(new ApiKeyModel()));
        when(virtualKeyValidator.checkCxIdAndUid(anyString(), anyString(), any())).thenReturn(Mono.just(new ApiKeyModel()));
        when(virtualKeyValidator.validateStateTransition(any(), any())).thenReturn(Mono.empty());
        when(virtualKeyValidator.validateNoOtherKeyWithSameStatus(anyString(), anyString(), anyString())).thenReturn(Mono.empty());
        when(apiKeyRepository.save(any(ApiKeyModel.class))).thenReturn(Mono.just(new ApiKeyModel()));

        Mono<Void> result = virtualKeyService.changeStatusVirtualKeys(xPagopaPnUid, CxTypeAuthFleetDto.PG, xPagopaPnCxId, xPagopaPnCxRole, id, requestDto, xPagopaPnCxGroups);

        StepVerifier.create(result)
                .verifyComplete();

        verify(apiKeyRepository, times(1)).findById(id);
        verify(apiKeyRepository, times(1)).save(any(ApiKeyModel.class));
        verify(virtualKeyValidator, times(1)).validateNoOtherKeyWithSameStatus(anyString(), anyString(), anyString());
    }

}
