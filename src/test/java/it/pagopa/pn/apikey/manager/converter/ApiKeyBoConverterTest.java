package it.pagopa.pn.apikey.manager.converter;

import it.pagopa.pn.apikey.manager.entity.ApiKeyHistoryModel;
import it.pagopa.pn.apikey.manager.entity.ApiKeyModel;
import it.pagopa.pn.apikey.manager.generated.openapi.rest.v1.aggregate.dto.ApiPdndDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ContextConfiguration(classes = {ApiKeyBoConverter.class})
@ExtendWith(SpringExtension.class)
class ApiKeyBoConverterTest {
    @Autowired
    private ApiKeyBoConverter apiKeyBoConverter;

    /**
     * Method under test: {@link ApiKeyBoConverter#convertToResponsePdnd(List, List)}
     */
    @Test
    void testConvertToResponsePdnd() {
        ArrayList<ApiPdndDto> apiPdndDtoList = new ArrayList<>();
        assertNull(apiKeyBoConverter.convertToResponsePdnd(apiPdndDtoList, new ArrayList<>()).getUnprocessedKey());
        assertTrue(apiPdndDtoList.isEmpty());
    }

    /**
     * Method under test: {@link ApiKeyBoConverter#convertToResponsePdnd(List, List)}
     */
    @Test
    void testConvertToResponsePdnd2() {
        ArrayList<ApiPdndDto> apiPdndDtoList = new ArrayList<>();
        apiPdndDtoList.add(new ApiPdndDto());
        assertEquals(1,
                apiKeyBoConverter.convertToResponsePdnd(apiPdndDtoList, new ArrayList<>()).getUnprocessedKey().size());
        assertEquals(1, apiPdndDtoList.size());
    }


    /**
     * Method under test: {@link ApiKeyBoConverter#convertToResponsePdnd(List, List)}
     */
    @Test
    void testConvertToResponsePdnd4() {
        ApiPdndDto apiPdndDto = mock(ApiPdndDto.class);
        when(apiPdndDto.getId()).thenReturn("42");

        ArrayList<ApiPdndDto> apiPdndDtoList = new ArrayList<>();
        apiPdndDtoList.add(apiPdndDto);
        assertEquals(1,
                apiKeyBoConverter.convertToResponsePdnd(apiPdndDtoList, new ArrayList<>()).getUnprocessedKey().size());
        verify(apiPdndDto).getId();
        assertEquals(1, apiPdndDtoList.size());
    }

    /**
     * Method under test: {@link ApiKeyBoConverter#convertToResponsePdnd(List, List)}
     */
    @Test
    void testConvertToResponsePdnd5() {
        ApiPdndDto apiPdndDto = mock(ApiPdndDto.class);
        when(apiPdndDto.getId()).thenReturn("42");

        ArrayList<ApiPdndDto> apiPdndDtoList = new ArrayList<>();
        apiPdndDtoList.add(apiPdndDto);

        ArrayList<ApiPdndDto> apiPdndDtoList1 = new ArrayList<>();
        apiPdndDtoList1.add(new ApiPdndDto());
        assertEquals(1,
                apiKeyBoConverter.convertToResponsePdnd(apiPdndDtoList, apiPdndDtoList1).getUnprocessedKey().size());
        verify(apiPdndDto).getId();
        assertEquals(1, apiPdndDtoList.size());
    }

    /**
     * Method under test: {@link ApiKeyBoConverter#convertResponsetoDto(List)}
     */
    @Test
    void testConvertResponsetoDto4() {
        ApiKeyModel apiKeyModel = mock(ApiKeyModel.class);
        when(apiKeyModel.getStatusHistory()).thenReturn(new ArrayList<>());
        when(apiKeyModel.getStatus()).thenReturn("CREATED");
        when(apiKeyModel.getGroups()).thenReturn(new ArrayList<>());
        when(apiKeyModel.getId()).thenReturn("42");
        when(apiKeyModel.getName()).thenReturn("Name");
        when(apiKeyModel.getVirtualKey()).thenReturn("Virtual Key");
        when(apiKeyModel.getLastUpdate()).thenReturn(LocalDateTime.of(1, 1, 1, 1, 1));

        ArrayList<ApiKeyModel> apiKeyModelList = new ArrayList<>();
        apiKeyModelList.add(apiKeyModel);
        assertEquals(1, apiKeyBoConverter.convertResponsetoDto(apiKeyModelList).getItems().size());
        verify(apiKeyModel).getId();
        verify(apiKeyModel).getName();
        verify(apiKeyModel).getStatus();
        verify(apiKeyModel).getVirtualKey();
        verify(apiKeyModel).getLastUpdate();
        verify(apiKeyModel).getGroups();
        verify(apiKeyModel).getStatusHistory();
    }

    /**
     * Method under test: {@link ApiKeyBoConverter#convertResponsetoDto(List)}
     */
    @Test
    void testConvertResponsetoDto5() {
        ApiKeyHistoryModel apiKeyHistoryModel = new ApiKeyHistoryModel();
        apiKeyHistoryModel.setChangeByDenomination("CREATED");
        apiKeyHistoryModel.setDate(LocalDateTime.of(1, 1, 1, 1, 1));
        apiKeyHistoryModel.setStatus("CREATED");

        ArrayList<ApiKeyHistoryModel> apiKeyHistoryModelList = new ArrayList<>();
        apiKeyHistoryModelList.add(apiKeyHistoryModel);
        ApiKeyModel apiKeyModel = mock(ApiKeyModel.class);
        when(apiKeyModel.getStatusHistory()).thenReturn(apiKeyHistoryModelList);
        when(apiKeyModel.getStatus()).thenReturn("CREATED");
        when(apiKeyModel.getGroups()).thenReturn(new ArrayList<>());
        when(apiKeyModel.getId()).thenReturn("42");
        when(apiKeyModel.getName()).thenReturn("Name");
        when(apiKeyModel.getVirtualKey()).thenReturn("Virtual Key");
        when(apiKeyModel.getLastUpdate()).thenReturn(LocalDateTime.of(1, 1, 1, 1, 1));

        ArrayList<ApiKeyModel> apiKeyModelList = new ArrayList<>();
        apiKeyModelList.add(apiKeyModel);
        assertEquals(1, apiKeyBoConverter.convertResponsetoDto(apiKeyModelList).getItems().size());
        verify(apiKeyModel).getId();
        verify(apiKeyModel).getName();
        verify(apiKeyModel).getStatus();
        verify(apiKeyModel).getVirtualKey();
        verify(apiKeyModel).getLastUpdate();
        verify(apiKeyModel).getGroups();
        verify(apiKeyModel).getStatusHistory();
    }
}

