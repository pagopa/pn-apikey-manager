package it.pagopa.pn.apikey.manager.converter;

import it.pagopa.pn.apikey.manager.entity.ApiKeyHistoryModel;
import it.pagopa.pn.apikey.manager.entity.ApiKeyModel;
import it.pagopa.pn.apikey.manager.generated.openapi.rest.v1.aggregate.dto.ApiPdndDto;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import software.amazon.awssdk.enhanced.dynamodb.model.Page;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.mock;

@ContextConfiguration(classes = {ApiKeyConverter.class})
@ExtendWith(MockitoExtension.class)
class ApiKeyConverterTest {

    @InjectMocks
    private ApiKeyConverter apiKeyConverter;


    /**
     * Method under test: {@link ApiKeyConverter#convertToResponsePdnd(List, List)}
     */
    @Test
    void testConvertToResponsePdnd4() {
        ArrayList<ApiPdndDto> apiPdndDtoList = new ArrayList<>();
        apiPdndDtoList.add(mock(ApiPdndDto.class));

        ArrayList<ApiPdndDto> apiPdndDtoList1 = new ArrayList<>();
        apiPdndDtoList1.add(new ApiPdndDto());
        assertEquals(1,
                apiKeyConverter.convertToResponsePdnd(apiPdndDtoList, apiPdndDtoList1).getApikeyNonModificate().size());
    }

    @Test
    void testConvertResponsetoDto() {
        List<ApiKeyModel> apiKeyModels = new ArrayList<>();
        List<String> groups = new ArrayList<>();
        List<ApiKeyHistoryModel> apiKeyHistories = new ArrayList<>();
        ApiKeyHistoryModel apiKeyHistoryModel = new ApiKeyHistoryModel();
        apiKeyHistoryModel.setChangeByDenomination("CREATE");
        apiKeyHistoryModel.setStatus("CREATED");
        apiKeyHistoryModel.setDate(LocalDateTime.now());
        apiKeyHistories.add(apiKeyHistoryModel);
        groups.add("RECLAMI");
        ApiKeyModel apiKeyModel = new ApiKeyModel();
        apiKeyModel.setId("id");
        apiKeyModel.setName("name");
        apiKeyModel.setLastUpdate(LocalDateTime.now());
        apiKeyModel.setVirtualKey("virtualKey");
        apiKeyModel.setGroups(groups);
        apiKeyModel.setStatus("CREATED");
        apiKeyModel.setUid("uuid");
        apiKeyModel.setCxId("cxId");
        apiKeyModel.setCorrelationId("correlationId");
        apiKeyModel.setCxType("cxType");
        apiKeyModel.setStatusHistory(apiKeyHistories);
        apiKeyModels.add(apiKeyModel);

        Map<String, AttributeValue> lastKey = new HashMap<>();
        lastKey.put("id", AttributeValue.builder().s("id").build());
        lastKey.put("lastUpdate", AttributeValue.builder().s("lastUpdate").build());

        Page<ApiKeyModel> page = Page.create(apiKeyModels, lastKey);

        Assertions.assertNotNull(apiKeyConverter.convertResponsetoDto(page, true));
    }


    @Test
    void testConvertResponsetoDtoExc2() {
        List<ApiKeyModel> apiKeyModels = new ArrayList<>();
        List<String> groups = new ArrayList<>();
        List<ApiKeyHistoryModel> apiKeyHistories = new ArrayList<>();
        ApiKeyHistoryModel apiKeyHistoryModel = new ApiKeyHistoryModel();
        apiKeyHistoryModel.setChangeByDenomination("CREATE");
        apiKeyHistoryModel.setStatus("CREATED");
        apiKeyHistories.add(apiKeyHistoryModel);
        groups.add("RECLAMI");
        ApiKeyModel apiKeyModel = new ApiKeyModel();
        apiKeyModel.setId("id");
        apiKeyModel.setName("name");
        apiKeyModel.setVirtualKey("virtualKey");
        apiKeyModel.setGroups(groups);
        apiKeyModel.setStatus("CREATED");
        apiKeyModel.setUid("uuid");
        apiKeyModel.setCxId("cxId");
        apiKeyModel.setCorrelationId("correlationId");
        apiKeyModel.setCxType("cxType");
        apiKeyModel.setStatusHistory(apiKeyHistories);
        apiKeyModels.add(apiKeyModel);

        Map<String, AttributeValue> lastKey = new HashMap<>();
        lastKey.put("id", AttributeValue.builder().s("id").build());
        lastKey.put("lastUpdate", AttributeValue.builder().s("lastUpdate").build());

        Page<ApiKeyModel> page = Page.create(apiKeyModels, lastKey);

        try {
            apiKeyConverter.convertResponsetoDto(page, true);
            fail("My method didn't throw when I expected it to");
        } catch (Exception expectedException) {
            System.out.println("Test passed");
        }
    }
}

