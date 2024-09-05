package it.pagopa.pn.apikey.manager.converter;

import it.pagopa.pn.apikey.manager.entity.ApiKeyModel;
import it.pagopa.pn.apikey.manager.generated.openapi.msclient.pndatavault.v1.dto.BaseRecipientDtoDto;
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

    public VirtualKeysResponseDto convertResponseToDto(Page<ApiKeyModel> pageApiKeyModels, Map<String, BaseRecipientDtoDto> mapBaseRecipient, Boolean showVirtualKey) {
        List<ApiKeyModel> apiKeyModels = pageApiKeyModels.items();

        VirtualKeysResponseDto virtualKeysResponseDto = new VirtualKeysResponseDto();

        List<VirtualKeyDto> virtualKeyDtos = getVirtualKeyDtosFromApiKeyModel(apiKeyModels, mapBaseRecipient, showVirtualKey);

        virtualKeysResponseDto.setItems(virtualKeyDtos);

        if (pageApiKeyModels.lastEvaluatedKey() != null) {
            Map<String, AttributeValue> lastKey = pageApiKeyModels.lastEvaluatedKey();
            virtualKeysResponseDto.setLastKey(lastKey.get("id").s());
            virtualKeysResponseDto.setLastUpdate(lastKey.get("lastUpdate").s());
        }

        return virtualKeysResponseDto;
    }

    private List<VirtualKeyDto> getVirtualKeyDtosFromApiKeyModel(List<ApiKeyModel> apiKeyModels, Map<String, BaseRecipientDtoDto> mapBaseRecipient, Boolean showVirtualKey) {
        List<VirtualKeyDto> virtualKeyDtos = new ArrayList<>();

        for (ApiKeyModel apiKeyModel : apiKeyModels) {
            virtualKeyDtos.add(getVirtualKeyDtoFromApiKeyModel(apiKeyModel, mapBaseRecipient, showVirtualKey));
        }

        return virtualKeyDtos;
    }

    private VirtualKeyDto getVirtualKeyDtoFromApiKeyModel(ApiKeyModel apiKeyModel, Map<String, BaseRecipientDtoDto> mapBaseRecipient, Boolean showVirtualKey) {
        VirtualKeyDto virtualKeyDto = new VirtualKeyDto();
        virtualKeyDto.setId(apiKeyModel.getId());
        virtualKeyDto.setName(apiKeyModel.getName());
        if (Boolean.TRUE.equals(showVirtualKey)) {
            virtualKeyDto.setValue(apiKeyModel.getVirtualKey());
        }
        if (apiKeyModel.getLastUpdate() != null)
            virtualKeyDto.setLastUpdate(from(apiKeyModel.getLastUpdate().toInstant(ZoneOffset.UTC)));

        virtualKeyDto.setStatus(VirtualKeyStatusDto.fromValue(apiKeyModel.getStatus()));

        if (mapBaseRecipient != null) {
            UserDtoDto user = new UserDtoDto();
            if (mapBaseRecipient.get(apiKeyModel.getUid()) != null) {
                user.setFiscalCode(mapBaseRecipient.get(apiKeyModel.getUid()).getTaxId());
                user.setDenomination(mapBaseRecipient.get(apiKeyModel.getUid()).getDenomination());
            }
            virtualKeyDto.setUser(user);
        }

        return virtualKeyDto;
    }

}
