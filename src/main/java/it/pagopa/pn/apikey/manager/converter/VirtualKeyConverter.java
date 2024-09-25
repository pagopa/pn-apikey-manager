package it.pagopa.pn.apikey.manager.converter;

import it.pagopa.pn.apikey.manager.apikey.manager.generated.openapi.msclient.pnexternalregistries.v1.dto.PgUserDetailDto;
import it.pagopa.pn.apikey.manager.entity.ApiKeyModel;
import it.pagopa.pn.apikey.manager.generated.openapi.server.v1.dto.UserDtoDto;
import it.pagopa.pn.apikey.manager.generated.openapi.server.v1.dto.VirtualKeyDto;
import it.pagopa.pn.apikey.manager.generated.openapi.server.v1.dto.VirtualKeyStatusDto;
import it.pagopa.pn.apikey.manager.generated.openapi.server.v1.dto.VirtualKeysResponseDto;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.enhanced.dynamodb.model.Page;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.util.Date.from;

@Component
public class VirtualKeyConverter {

    public VirtualKeysResponseDto convertResponseToDto(Page<ApiKeyModel> pageApiKeyModels, Map<String, PgUserDetailDto> mapPgUserDetail, Boolean showVirtualKey) {
        List<ApiKeyModel> apiKeyModels = pageApiKeyModels.items();

        VirtualKeysResponseDto virtualKeysResponseDto = new VirtualKeysResponseDto();

        List<VirtualKeyDto> virtualKeyDtos = getVirtualKeyDtosFromApiKeyModel(apiKeyModels, mapPgUserDetail, showVirtualKey);

        virtualKeysResponseDto.setItems(virtualKeyDtos);

        if (pageApiKeyModels.lastEvaluatedKey() != null) {
            Map<String, AttributeValue> lastKey = pageApiKeyModels.lastEvaluatedKey();
            virtualKeysResponseDto.setLastKey(lastKey.get("id").s());
            virtualKeysResponseDto.setLastUpdate(lastKey.get("lastUpdate").s());
        }

        return virtualKeysResponseDto;
    }

    private List<VirtualKeyDto> getVirtualKeyDtosFromApiKeyModel(List<ApiKeyModel> apiKeyModels, Map<String, PgUserDetailDto> mapPgUserDetail, Boolean showVirtualKey) {
        List<VirtualKeyDto> virtualKeyDtos = new ArrayList<>();

        for (ApiKeyModel apiKeyModel : apiKeyModels) {
            virtualKeyDtos.add(getVirtualKeyDtoFromApiKeyModel(apiKeyModel, mapPgUserDetail, showVirtualKey));
        }

        return virtualKeyDtos;
    }

    private VirtualKeyDto getVirtualKeyDtoFromApiKeyModel(ApiKeyModel apiKeyModel, Map<String, PgUserDetailDto> mapPgUserDetail, Boolean showVirtualKey) {
        VirtualKeyDto virtualKeyDto = new VirtualKeyDto();
        virtualKeyDto.setId(apiKeyModel.getId());
        virtualKeyDto.setName(apiKeyModel.getName());
        if (Boolean.TRUE.equals(showVirtualKey)) {
            virtualKeyDto.setValue(apiKeyModel.getVirtualKey());
        }
        if (apiKeyModel.getLastUpdate() != null)
            virtualKeyDto.setLastUpdate(from(apiKeyModel.getLastUpdate().toInstant(ZoneOffset.UTC)));

        virtualKeyDto.setStatus(VirtualKeyStatusDto.fromValue(apiKeyModel.getStatus()));

        if (mapPgUserDetail != null) {
            UserDtoDto user = new UserDtoDto();
            if (mapPgUserDetail.get(apiKeyModel.getUid()) != null) {
                user.setFiscalCode(mapPgUserDetail.get(apiKeyModel.getUid()).getTaxCode());
                user.setDenomination(mapPgUserDetail.get(apiKeyModel.getUid()).getName() + " " + mapPgUserDetail.get(apiKeyModel.getUid()).getSurname());
            }
            virtualKeyDto.setUser(user);
        }

        return virtualKeyDto;
    }

}
