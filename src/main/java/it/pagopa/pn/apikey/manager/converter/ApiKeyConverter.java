package it.pagopa.pn.apikey.manager.converter;

import it.pagopa.pn.apikey.manager.entity.ApiKeyHistory;
import it.pagopa.pn.apikey.manager.entity.ApiKeyModel;
import it.pagopa.pn.apikey.manager.generated.openapi.rest.v1.dto.ApiKeyRowDto;
import it.pagopa.pn.apikey.manager.generated.openapi.rest.v1.dto.ApiKeyStatusDto;
import it.pagopa.pn.apikey.manager.generated.openapi.rest.v1.dto.ApiKeyStatusHistoryDto;
import it.pagopa.pn.apikey.manager.generated.openapi.rest.v1.dto.ApiKeysResponseDto;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.enhanced.dynamodb.model.Page;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class ApiKeyConverter {

    public ApiKeysResponseDto convertResponsetoDto(Page<ApiKeyModel> pageApiKeyModels, Boolean showVirtualKey){

        List<ApiKeyModel> apiKeyModels = pageApiKeyModels.items();

        ApiKeysResponseDto apiKeysResponseDto = new ApiKeysResponseDto();

        List<ApiKeyRowDto> apiKeyRowDtos = getApiKeyRowDtosFromApiKeyModel(apiKeyModels,showVirtualKey);

        apiKeysResponseDto.setItems(apiKeyRowDtos);

        if(pageApiKeyModels.lastEvaluatedKey()!=null){
            Map<String, AttributeValue> lastKey = pageApiKeyModels.lastEvaluatedKey();
            apiKeysResponseDto.setLastKey(lastKey.get("id").s());
            apiKeysResponseDto.setLastUpdate(lastKey.get("lastUpdate").s());
        }

        return apiKeysResponseDto;
    }

    @SneakyThrows
    private List<ApiKeyRowDto> getApiKeyRowDtosFromApiKeyModel(List<ApiKeyModel> apiKeyModels, Boolean showVirtualKey){
        List<ApiKeyRowDto> apiKeyRowDtos = new ArrayList<>();

        for(ApiKeyModel apiKeyModel : apiKeyModels){
            apiKeyRowDtos.add(getApiKeyRowDtoFromApiKeyModel(apiKeyModel,showVirtualKey));
        }

        return apiKeyRowDtos;
    }

    @SneakyThrows
    private ApiKeyRowDto getApiKeyRowDtoFromApiKeyModel(ApiKeyModel apiKeyModel, Boolean showVirtualKey){
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");

        ApiKeyRowDto apiKeyRowDto = new ApiKeyRowDto();
        apiKeyRowDto.setId(apiKeyModel.getId());
        apiKeyRowDto.setName(apiKeyModel.getName());
        if(showVirtualKey){
            apiKeyRowDto.setValue(apiKeyModel.getVirtualKey());
        }
        apiKeyRowDto.setLastUpdate(df.parse(apiKeyModel.getLastUpdate()));
        apiKeyRowDto.setGroups(apiKeyModel.getGroups());
        apiKeyRowDto.setStatus(ApiKeyStatusDto.fromValue(apiKeyModel.getStatus()));
        for(ApiKeyHistory apiKeyHistory : apiKeyModel.getStatusHistory()){
            apiKeyRowDto.addStatusHistoryItem(getApiKeyStatusHistoryDtoFromApiKeyHistory(apiKeyHistory));
        }
        return apiKeyRowDto;
    }

    @SneakyThrows
    private ApiKeyStatusHistoryDto getApiKeyStatusHistoryDtoFromApiKeyHistory(ApiKeyHistory apiKeyHistory){
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");

        ApiKeyStatusHistoryDto apiKeyStatusHistoryDto = new ApiKeyStatusHistoryDto();
        apiKeyStatusHistoryDto.setStatus(ApiKeyStatusDto.fromValue(apiKeyHistory.getStatus()));
        apiKeyStatusHistoryDto.setDate(df.parse(apiKeyHistory.getDate()));
        apiKeyStatusHistoryDto.setChangedByDenomination(apiKeyHistory.getChangeByDenomination());

        return apiKeyStatusHistoryDto;
    }
}
