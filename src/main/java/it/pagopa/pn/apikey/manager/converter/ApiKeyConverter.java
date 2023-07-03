package it.pagopa.pn.apikey.manager.converter;

import it.pagopa.pn.apikey.manager.entity.ApiKeyHistoryModel;
import it.pagopa.pn.apikey.manager.entity.ApiKeyModel;
import it.pagopa.pn.apikey.manager.generated.openapi.server.v1.dto.ApiKeyRowDto;
import it.pagopa.pn.apikey.manager.generated.openapi.server.v1.dto.ApiKeyStatusDto;
import it.pagopa.pn.apikey.manager.generated.openapi.server.v1.dto.ApiKeyStatusHistoryDto;
import it.pagopa.pn.apikey.manager.generated.openapi.server.v1.dto.ApiKeysResponseDto;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.enhanced.dynamodb.model.Page;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.util.Date.from;

@Component
public class ApiKeyConverter {

    public ApiKeysResponseDto convertResponseToDto(Page<ApiKeyModel> pageApiKeyModels, Boolean showVirtualKey) {
        List<ApiKeyModel> apiKeyModels = pageApiKeyModels.items();

        ApiKeysResponseDto apiKeysResponseDto = new ApiKeysResponseDto();

        List<ApiKeyRowDto> apiKeyRowDtos = getApiKeyRowDtosFromApiKeyModel(apiKeyModels, showVirtualKey);

        apiKeysResponseDto.setItems(apiKeyRowDtos);

        if (pageApiKeyModels.lastEvaluatedKey() != null) {
            Map<String, AttributeValue> lastKey = pageApiKeyModels.lastEvaluatedKey();
            apiKeysResponseDto.setLastKey(lastKey.get("id").s());
            apiKeysResponseDto.setLastUpdate(lastKey.get("lastUpdate").s());
        }

        return apiKeysResponseDto;
    }

    private List<ApiKeyRowDto> getApiKeyRowDtosFromApiKeyModel(List<ApiKeyModel> apiKeyModels, Boolean showVirtualKey) {
        List<ApiKeyRowDto> apiKeyRowDtos = new ArrayList<>();

        for (ApiKeyModel apiKeyModel : apiKeyModels) {
            apiKeyRowDtos.add(getApiKeyRowDtoFromApiKeyModel(apiKeyModel, showVirtualKey));
        }

        return apiKeyRowDtos;
    }

    private ApiKeyRowDto getApiKeyRowDtoFromApiKeyModel(ApiKeyModel apiKeyModel, Boolean showVirtualKey) {
        ApiKeyRowDto apiKeyRowDto = new ApiKeyRowDto();
        apiKeyRowDto.setId(apiKeyModel.getId());
        apiKeyRowDto.setName(apiKeyModel.getName());
        if (Boolean.TRUE.equals(showVirtualKey)) {
            apiKeyRowDto.setValue(apiKeyModel.getVirtualKey());
        }
        apiKeyRowDto.setLastUpdate(from(apiKeyModel.getLastUpdate().toInstant(ZoneOffset.UTC)));
        apiKeyRowDto.setGroups(apiKeyModel.getGroups());
        apiKeyRowDto.setStatus(ApiKeyStatusDto.fromValue(apiKeyModel.getStatus()));
        for (ApiKeyHistoryModel apiKeyHistoryModel : apiKeyModel.getStatusHistory()) {
            apiKeyRowDto.addStatusHistoryItem(getApiKeyStatusHistoryDtoFromApiKeyHistory(apiKeyHistoryModel));
        }
        return apiKeyRowDto;
    }

    private ApiKeyStatusHistoryDto getApiKeyStatusHistoryDtoFromApiKeyHistory(ApiKeyHistoryModel apiKeyHistoryModel) {
        ApiKeyStatusHistoryDto apiKeyStatusHistoryDto = new ApiKeyStatusHistoryDto();
        apiKeyStatusHistoryDto.setStatus(ApiKeyStatusDto.fromValue(apiKeyHistoryModel.getStatus()));

        apiKeyStatusHistoryDto.setDate(from(apiKeyHistoryModel.getDate().toInstant(ZoneOffset.UTC)));
        apiKeyStatusHistoryDto.setChangedByDenomination(apiKeyHistoryModel.getChangeByDenomination());

        return apiKeyStatusHistoryDto;
    }
}
