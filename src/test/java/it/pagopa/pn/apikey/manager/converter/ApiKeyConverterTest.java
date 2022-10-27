package it.pagopa.pn.apikey.manager.converter;

import it.pagopa.pn.apikey.manager.entity.ApiKeyHistory;
import it.pagopa.pn.apikey.manager.entity.ApiKeyModel;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.enhanced.dynamodb.model.Page;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.fail;

@ExtendWith(MockitoExtension.class)
class ApiKeyConverterTest {

    @InjectMocks
    private ApiKeyConverter apiKeyConverter;

    @Test
    void testConvertResponsetoDto() {
        List<ApiKeyModel> apiKeyModels = new ArrayList<>();
        List<String> groups = new ArrayList<>();
        List<ApiKeyHistory> apiKeyHistories = new ArrayList<>();
        ApiKeyHistory apiKeyHistory = new ApiKeyHistory();
        apiKeyHistory.setChangeByDenomination("CREATE");
        apiKeyHistory.setStatus("CREATED");
        apiKeyHistories.add(apiKeyHistory);
        groups.add("RECLAMI");
        ApiKeyModel apiKeyModel = new ApiKeyModel();
        apiKeyModel.setId("id");
        apiKeyModel.setApiKey("apiKey");
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
        lastKey.put("id",AttributeValue.builder().s("id").build());
        lastKey.put("lastUpdate",AttributeValue.builder().s("lastUpdate").build());

        Page<ApiKeyModel> page = Page.create(apiKeyModels,lastKey);

        Assertions.assertNotNull(apiKeyConverter.convertResponsetoDto(page, true));
    }


    @Test
    void testConvertResponsetoDtoExc2() {
        List<ApiKeyModel> apiKeyModels = new ArrayList<>();
        List<String> groups = new ArrayList<>();
        List<ApiKeyHistory> apiKeyHistories = new ArrayList<>();
        ApiKeyHistory apiKeyHistory = new ApiKeyHistory();
        apiKeyHistory.setChangeByDenomination("CREATE");
        apiKeyHistory.setStatus("CREATED");
        apiKeyHistories.add(apiKeyHistory);
        groups.add("RECLAMI");
        ApiKeyModel apiKeyModel = new ApiKeyModel();
        apiKeyModel.setId("id");
        apiKeyModel.setApiKey("apiKey");
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
        lastKey.put("id",AttributeValue.builder().s("id").build());
        lastKey.put("lastUpdate",AttributeValue.builder().s("lastUpdate").build());

        Page<ApiKeyModel> page = Page.create(apiKeyModels,lastKey);

        try{
            apiKeyConverter.convertResponsetoDto(page, true);
            fail( "My method didn't throw when I expected it to" );
        } catch (Exception expectedException) {
            System.out.println("Test passed");
        }
    }
}

