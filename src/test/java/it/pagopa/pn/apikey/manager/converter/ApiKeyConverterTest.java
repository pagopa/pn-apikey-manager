package it.pagopa.pn.apikey.manager.converter;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import it.pagopa.pn.apikey.manager.entity.ApiKeyHistory;
import it.pagopa.pn.apikey.manager.entity.ApiKeyModel;

import java.util.ArrayList;
import java.util.List;


@ExtendWith(MockitoExtension.class)
class ApiKeyConverterTest {

    @InjectMocks
    private ApiKeyConverter apiKeyConverter;

    private static List<ApiKeyModel> apiKeyModels;
    private static ApiKeyModel apiKeyModel;

    private static List<ApiKeyHistory> apiKeyHistories;

    @BeforeAll
    static void setup(){
        apiKeyModels = new ArrayList<>();
        List<String> groups = new ArrayList<>();
        apiKeyHistories = new ArrayList<>();
        ApiKeyHistory apiKeyHistory = new ApiKeyHistory();
        apiKeyHistory.setChangeByDenomination("CREATE");
        apiKeyHistory.setDate("2022-10-07T14:46:26.869Z");
        apiKeyHistory.setStatus("CREATED");
        apiKeyHistories.add(apiKeyHistory);
        groups.add("RECLAMI");
        apiKeyModel = new ApiKeyModel();
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
        apiKeyModel.setLastUpdate("2022-10-07T14:46:26.869Z");
        apiKeyModel.setStatusHistory(apiKeyHistories);
        apiKeyModels.add(apiKeyModel);
    }

    @Test
    void testConvertResponsetoDto() {
        apiKeyModel.setLastUpdate("2022-10-07T14:46:26.869Z");
        assertNotNull(apiKeyConverter.convertResponsetoDto(apiKeyModels));
    }

    @Test
    void testConvertResponsetoDtoExc() {
        apiKeyModel.setLastUpdate("error");
        try{
            apiKeyConverter.convertResponsetoDto(apiKeyModels);
            fail( "My method didn't throw when I expected it to" );
        } catch (Exception expectedException) {
            System.out.println("Test passed");
        }
    }

    @Test
    void testConvertResponsetoDtoExc2() {
        ApiKeyHistory apiKeyHistory = new ApiKeyHistory();
        apiKeyHistory.setChangeByDenomination("CREATE");
        apiKeyHistory.setDate("error");
        apiKeyHistory.setStatus("CREATED");
        apiKeyHistories.add(apiKeyHistory);
        try{
            apiKeyConverter.convertResponsetoDto(apiKeyModels);
            fail( "My method didn't throw when I expected it to" );
        } catch (Exception expectedException) {
            System.out.println("Test passed");
        }
    }

}

