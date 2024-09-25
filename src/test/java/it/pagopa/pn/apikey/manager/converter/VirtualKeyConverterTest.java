package it.pagopa.pn.apikey.manager.converter;

import it.pagopa.pn.apikey.manager.apikey.manager.generated.openapi.msclient.pnexternalregistries.v1.dto.PgUserDetailDto;
import it.pagopa.pn.apikey.manager.entity.ApiKeyModel;
import it.pagopa.pn.apikey.manager.generated.openapi.server.v1.dto.VirtualKeyStatusDto;
import it.pagopa.pn.apikey.manager.generated.openapi.server.v1.dto.VirtualKeysResponseDto;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.enhanced.dynamodb.model.Page;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class VirtualKeyConverterTest {
    private final VirtualKeyConverter virtualKeyConverter = new VirtualKeyConverter();

    @Test
    void convertResponseToDto_shouldReturnVirtualKeysResponseDto_withValidData() {
        ApiKeyModel apiKeyModel = new ApiKeyModel();
        apiKeyModel.setId("id");
        apiKeyModel.setName("name");
        apiKeyModel.setVirtualKey("virtualKey");
        apiKeyModel.setLastUpdate(LocalDateTime.now());
        apiKeyModel.setStatus("ENABLED");
        apiKeyModel.setUid("uid");

        PgUserDetailDto pgUserDetailDto = new PgUserDetailDto();
        pgUserDetailDto.setId("id");
        pgUserDetailDto.setName("name");
        pgUserDetailDto.setSurname("surname");
        pgUserDetailDto.setTaxCode("taxCode");

        Page<ApiKeyModel> page = Page.create(List.of(apiKeyModel));
        Map<String, PgUserDetailDto> mapPgUserDetail = Map.of("uid", pgUserDetailDto);

        VirtualKeysResponseDto responseDto = virtualKeyConverter.convertResponseToDto(page, mapPgUserDetail, true);

        assertEquals(1, responseDto.getItems().size());
        assertEquals("id", responseDto.getItems().get(0).getId());
        assertEquals("name", responseDto.getItems().get(0).getName());
        assertEquals("virtualKey", responseDto.getItems().get(0).getValue());
        assertNotNull(responseDto.getItems().get(0).getLastUpdate());
        assertEquals(VirtualKeyStatusDto.ENABLED, responseDto.getItems().get(0).getStatus());
        assertEquals("taxCode", responseDto.getItems().get(0).getUser().getFiscalCode());
        assertEquals("name surname", responseDto.getItems().get(0).getUser().getDenomination());
    }

    @Test
    void convertResponseToDto_shouldReturnEmptyItems_whenNoApiKeyModels() {
        Page<ApiKeyModel> page = Page.create(List.of());
        Map<String, PgUserDetailDto> mapPgUserDetail = Map.of();

        VirtualKeysResponseDto responseDto = virtualKeyConverter.convertResponseToDto(page, mapPgUserDetail, true);

        assertTrue(responseDto.getItems().isEmpty());
    }

    @Test
    void convertResponseToDto_shouldNotIncludeVirtualKey_whenShowVirtualKeyIsFalse() {
        ApiKeyModel apiKeyModel = new ApiKeyModel();
        apiKeyModel.setId("id");
        apiKeyModel.setName("name");
        apiKeyModel.setVirtualKey("virtualKey");
        apiKeyModel.setLastUpdate(LocalDateTime.now());
        apiKeyModel.setStatus("ENABLED");
        apiKeyModel.setUid("uid");

        PgUserDetailDto pgUserDetailDto = new PgUserDetailDto();
        pgUserDetailDto.setId("id");
        pgUserDetailDto.setName("name");
        pgUserDetailDto.setSurname("surname");
        pgUserDetailDto.setTaxCode("taxCode");

        Page<ApiKeyModel> page = Page.create(List.of(apiKeyModel));
        Map<String, PgUserDetailDto> mapPgUserDetail = Map.of("uid", pgUserDetailDto);

        VirtualKeysResponseDto responseDto = virtualKeyConverter.convertResponseToDto(page, mapPgUserDetail, false);

        assertEquals(1, responseDto.getItems().size());
        assertEquals("id", responseDto.getItems().get(0).getId());
        assertEquals("name", responseDto.getItems().get(0).getName());
        assertNull(responseDto.getItems().get(0).getValue());
        assertNotNull(responseDto.getItems().get(0).getLastUpdate());
        assertEquals(VirtualKeyStatusDto.ENABLED, responseDto.getItems().get(0).getStatus());
        assertEquals("taxCode", responseDto.getItems().get(0).getUser().getFiscalCode());
        assertEquals("name surname", responseDto.getItems().get(0).getUser().getDenomination());
    }

    @Test
    void convertResponseToDto_shouldSetLastKeyAndLastUpdate_whenPageHasLastEvaluatedKey() {
        ApiKeyModel apiKeyModel = new ApiKeyModel();
        apiKeyModel.setId("id");
        apiKeyModel.setName("name");
        apiKeyModel.setVirtualKey("virtualKey");
        apiKeyModel.setLastUpdate(LocalDateTime.now());
        apiKeyModel.setStatus("ENABLED");
        apiKeyModel.setUid("uid");

        PgUserDetailDto pgUserDetailDto = new PgUserDetailDto();
        pgUserDetailDto.setId("id");
        pgUserDetailDto.setName("name");
        pgUserDetailDto.setSurname("surname");
        pgUserDetailDto.setTaxCode("taxCode");

        Map<String, AttributeValue> lastEvaluatedKey = Map.of(
                "id", AttributeValue.builder().s("lastKeyId").build(),
                "lastUpdate", AttributeValue.builder().s("lastKeyUpdate").build()
        );

        Page<ApiKeyModel> page = Page.create(List.of(apiKeyModel), lastEvaluatedKey);
        Map<String, PgUserDetailDto> mapPgUserDetail = Map.of("uid", pgUserDetailDto);

        VirtualKeysResponseDto responseDto = virtualKeyConverter.convertResponseToDto(page, mapPgUserDetail, true);

        assertEquals("lastKeyId", responseDto.getLastKey());
        assertEquals("lastKeyUpdate", responseDto.getLastUpdate());
    }
}