package it.pagopa.pn.apikey.manager.converter;

import it.pagopa.pn.apikey.manager.entity.ApiKeyModel;
import it.pagopa.pn.apikey.manager.generated.openapi.rest.v1.aggregate.dto.ApiKeyRowDto;
import it.pagopa.pn.apikey.manager.generated.openapi.rest.v1.aggregate.dto.ApiKeyStatusDto;
import it.pagopa.pn.apikey.manager.generated.openapi.rest.v1.aggregate.dto.ApiPdndDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ContextConfiguration(classes = {ApiKeyBoConverter.class})
@ExtendWith(SpringExtension.class)
class ApiKeyBoConverterTest {

    @Autowired
    private ApiKeyBoConverter apiKeyBoConverter;

    /**
     * Method under test: {@link ApiKeyBoConverter#convertToResponsePdnd(Collection, List)}
     */
    @Test
    void testConvertToResponsePdnd() {
        ArrayList<ApiPdndDto> apiPdndDtoList = new ArrayList<>();
        assertTrue(apiKeyBoConverter.convertToResponsePdnd(apiPdndDtoList, new ArrayList<>()).getUnprocessedKey().isEmpty());
        assertTrue(apiPdndDtoList.isEmpty());
    }

    /**
     * Method under test: {@link ApiKeyBoConverter#convertToResponsePdnd(Collection, List)}
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
     * Method under test: {@link ApiKeyBoConverter#convertToResponsePdnd(Collection, List)}
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
     * Method under test: {@link ApiKeyBoConverter#convertToResponsePdnd(Collection, List)}
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
     * Method under test: {@link ApiKeyBoConverter#convertResponseToDto(List)}
     */
    @Test
    void testConvertResponsetoDto5() {
        ApiKeyModel apiKeyModel = mock(ApiKeyModel.class);
        when(apiKeyModel.isPdnd()).thenReturn(true);
        when(apiKeyModel.getId()).thenReturn("42");
        when(apiKeyModel.getName()).thenReturn("Name");
        when(apiKeyModel.getStatus()).thenReturn("CREATED");
        when(apiKeyModel.getGroups()).thenReturn(new ArrayList<>());

        ArrayList<ApiKeyModel> apiKeyModelList = new ArrayList<>();
        apiKeyModelList.add(apiKeyModel);
        List<ApiKeyRowDto> items = apiKeyBoConverter.convertResponseToDto(apiKeyModelList).getItems();
        assertEquals(1, items.size());
        ApiKeyRowDto getResult = items.get(0);
        assertTrue(getResult.getGroups().isEmpty());
        assertEquals(ApiKeyStatusDto.CREATED, getResult.getStatus());
        assertTrue(getResult.getPdnd());
        assertEquals("Name", getResult.getName());
        assertEquals("42", getResult.getId());
        verify(apiKeyModel).isPdnd();
        verify(apiKeyModel).getId();
        verify(apiKeyModel).getName();
        verify(apiKeyModel).getStatus();
        verify(apiKeyModel).getGroups();
    }
}
