package it.pagopa.pn.apikey.manager.converter;

import it.pagopa.pn.apikey.manager.entity.ApiKeyModel;
import it.pagopa.pn.apikey.manager.generated.openapi.rest.v1.aggregate.dto.*;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;

@Component
public class ApiKeyBoConverter {

    public ResponsePdndDto convertToResponsePdnd(Collection<ApiPdndDto> apiPdndDtos, List<ApiPdndDto> apiPdndChangedDtos) {
        apiPdndDtos.removeIf(apiPdndChangedDtos::contains);

        ResponsePdndDto responsePdndDto = new ResponsePdndDto();

        List<String> unprocessedKey = apiPdndDtos.stream().map(ApiPdndDto::getId).toList();
        responsePdndDto.setUnprocessedKey(unprocessedKey);

        return responsePdndDto;
    }

    public ResponseApiKeysDto convertResponseToDto(List<ApiKeyModel> apiKeyModels) {
        ResponseApiKeysDto responseApiKeysDto = new ResponseApiKeysDto();

        List<ApiKeyRowDto> apiKeyRowDtos = getApiKeyRowDtosFromApiKeyModel(apiKeyModels);

        responseApiKeysDto.setItems(apiKeyRowDtos);
        responseApiKeysDto.setTotal(apiKeyModels.size());

        return responseApiKeysDto;
    }

    private List<ApiKeyRowDto> getApiKeyRowDtosFromApiKeyModel(List<ApiKeyModel> apiKeyModels) {
        return apiKeyModels.stream().map(this::getApiKeyRowDtoFromApiKeyModel).toList();
    }

    private ApiKeyRowDto getApiKeyRowDtoFromApiKeyModel(ApiKeyModel apiKeyModel) {
        ApiKeyRowDto apiKeyRowDto = new ApiKeyRowDto();
        apiKeyRowDto.setId(apiKeyModel.getId());
        apiKeyRowDto.setName(apiKeyModel.getName());
        apiKeyRowDto.setPdnd(apiKeyModel.isPdnd());
        apiKeyRowDto.setGroups(apiKeyModel.getGroups());
        apiKeyRowDto.setStatus(ApiKeyStatusDto.fromValue(apiKeyModel.getStatus()));
        return apiKeyRowDto;
    }

}
