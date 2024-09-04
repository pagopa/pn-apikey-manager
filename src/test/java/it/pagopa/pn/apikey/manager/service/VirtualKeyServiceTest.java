package it.pagopa.pn.apikey.manager.service;


import it.pagopa.pn.apikey.manager.entity.ApiKeyHistoryModel;
import it.pagopa.pn.apikey.manager.entity.ApiKeyModel;
import it.pagopa.pn.apikey.manager.exception.ApiKeyManagerException;
import it.pagopa.pn.apikey.manager.generated.openapi.server.v1.dto.CxTypeAuthFleetDto;
import it.pagopa.pn.apikey.manager.generated.openapi.server.v1.dto.VirtualKeyStatusDto;
import it.pagopa.pn.apikey.manager.repository.ApiKeyRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest
@ExtendWith(SpringExtension.class)
class VirtualKeyServiceTest {

    @Autowired
    private VirtualKeyService virtualKeyService;

    @MockBean
    private ApiKeyRepository apiKeyRepository;

    List<String> xPagopaPnCxGroups = new ArrayList<>();

    @Test
    void deleteVirtualKey_InternalErrorTest() {
        ApiKeyModel apiKeyModel = new ApiKeyModel();
        apiKeyModel.setId("id");
        apiKeyModel.setVirtualKey("id");
        apiKeyModel.setStatus(String.valueOf(VirtualKeyStatusDto.BLOCKED));
        apiKeyModel.setCxId("xPagopaPnCxId");
        apiKeyModel.setCxGroup(xPagopaPnCxGroups);
        apiKeyModel.setName("name");
        apiKeyModel.setPdnd(true);
        apiKeyModel.setUid("xPagopaPnUid");
        apiKeyModel.setCxType(CxTypeAuthFleetDto.PG.toString());

        ApiKeyHistoryModel apiKeyHistoryModel = new ApiKeyHistoryModel();
        apiKeyHistoryModel.setStatus("DELETED");
        apiKeyHistoryModel.setDate(LocalDateTime.now());
        apiKeyHistoryModel.setChangeByDenomination("xPagopaPnUid");

        List<ApiKeyHistoryModel> apiKeyHistoryModelList = new ArrayList<>(apiKeyModel.getStatusHistory());
        apiKeyHistoryModelList.add(apiKeyHistoryModel);

        apiKeyModel.setStatusHistory(apiKeyHistoryModelList);
        when(apiKeyRepository.findById("id")).thenReturn(Mono.just(apiKeyModel));
        StepVerifier.create(virtualKeyService.deleteVirtualKey("id", "xPagopaPnUid", CxTypeAuthFleetDto.PG, "xPagopaPnCxId", xPagopaPnCxGroups, "xPagopaPnCxRole"))
                .expectErrorMatches(throwable -> throwable instanceof ApiKeyManagerException && throwable.getMessage().contains("Internal error"));
    }

    @Test
    void deleteVirtualKey_validateRoleErrorTest() {
        ApiKeyModel apiKeyModel = new ApiKeyModel();
        apiKeyModel.setId("id");
        apiKeyModel.setVirtualKey("id");
        apiKeyModel.setStatus(String.valueOf(VirtualKeyStatusDto.BLOCKED));
        apiKeyModel.setCxId("xPagopaPnCxId");
        apiKeyModel.setCxGroup(xPagopaPnCxGroups);
        apiKeyModel.setName("name");
        apiKeyModel.setPdnd(true);
        apiKeyModel.setUid("xPagopaPnUid");
        apiKeyModel.setCxType(CxTypeAuthFleetDto.PG.toString());

        ApiKeyHistoryModel apiKeyHistoryModel = new ApiKeyHistoryModel();
        apiKeyHistoryModel.setStatus("DELETED");
        apiKeyHistoryModel.setDate(LocalDateTime.now());
        apiKeyHistoryModel.setChangeByDenomination("xPagopaPnUid");

        List<ApiKeyHistoryModel> apiKeyHistoryModelList = new ArrayList<>(apiKeyModel.getStatusHistory());
        apiKeyHistoryModelList.add(apiKeyHistoryModel);

        apiKeyModel.setStatusHistory(apiKeyHistoryModelList);

        when(apiKeyRepository.findById("id")).thenReturn(Mono.just(apiKeyModel));
        StepVerifier.create(virtualKeyService.deleteVirtualKey("id", "xPagopaPnUid", CxTypeAuthFleetDto.PG, "xPagopaPnCxId", xPagopaPnCxGroups, "xPagopaPnCxRole"))
                .expectErrorMatches(throwable -> throwable instanceof ApiKeyManagerException && throwable.getMessage().contains("Can not delete ApiKey - current state is"));
    }

    @Test
    void deleteVirtualKey_CxTypeNotAllowedTest() {
        StepVerifier.create(virtualKeyService.deleteVirtualKey("id", "xPagopaPnUid", CxTypeAuthFleetDto.PA, "xPagopaPnCxId", xPagopaPnCxGroups, "xPagopaPnCxRole"))
                .expectErrorMatches(throwable -> throwable instanceof ApiKeyManagerException && throwable.getMessage().contains("CxTypeAuthFleet PA not allowed"));
    }

    @Test
    void deleteVirtualKey_SuccessTest() {
        ApiKeyModel apiKeyModel = new ApiKeyModel();
        apiKeyModel.setId("id");
        apiKeyModel.setVirtualKey("id");
        apiKeyModel.setStatus(String.valueOf(VirtualKeyStatusDto.BLOCKED));
        apiKeyModel.setCxId("xPagopaPnCxId");
        apiKeyModel.setCxGroup(xPagopaPnCxGroups);
        apiKeyModel.setName("name");
        apiKeyModel.setPdnd(true);
        apiKeyModel.setUid("xPagopaPnUid");
        apiKeyModel.setCxType(CxTypeAuthFleetDto.PG.toString());

        ApiKeyHistoryModel apiKeyHistoryModel = new ApiKeyHistoryModel();
        apiKeyHistoryModel.setStatus("DELETED");
        apiKeyHistoryModel.setDate(LocalDateTime.now());
        apiKeyHistoryModel.setChangeByDenomination("xPagopaPnUid");

        List<ApiKeyHistoryModel> apiKeyHistoryModelList = new ArrayList<>(apiKeyModel.getStatusHistory());
        apiKeyHistoryModelList.add(apiKeyHistoryModel);

        apiKeyModel.setStatusHistory(apiKeyHistoryModelList);

        when(apiKeyRepository.findById("id")).thenReturn(Mono.just(apiKeyModel));
        when(apiKeyRepository.save(any())).thenReturn(Mono.just(apiKeyModel));
        StepVerifier.create(virtualKeyService.deleteVirtualKey("id", "xPagopaPnUid", CxTypeAuthFleetDto.PG, "xPagopaPnCxId", xPagopaPnCxGroups, "xPagopaPnCxRole"))
                .expectNextMatches(response -> response.equals("VirtualKey deleted"))
                .verifyComplete();
    }




}