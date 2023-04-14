package it.pagopa.pn.apikey.manager.converter;

import it.pagopa.pn.apikey.manager.entity.ApiKeyModel;
import it.pagopa.pn.apikey.manager.generated.openapi.rest.v1.aggregate.dto.*;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ApiKeyBoConverter {

    public ResponsePdndDto convertToResponsePdnd(Collection<ApiPdndDto> apiPdndDtos, List<ApiPdndDto> apiPdndChangedDtos){
        apiPdndDtos.removeIf(apiPdndChangedDtos::contains);

        ResponsePdndDto apiKeyResponsePdndDto = new ResponsePdndDto();
        if(!apiPdndDtos.isEmpty()){
            List<String> unprocessedKey = apiPdndDtos.stream().map(ApiPdndDto::getId).collect(Collectors.toList());
            apiKeyResponsePdndDto.setUnprocessedKey(unprocessedKey);
        }

        return apiKeyResponsePdndDto;
    }

    public ResponseApiKeysDto convertResponsetoDto(List<ApiKeyModel> apiKeyModels){

        ResponseApiKeysDto responseApiKeysDto = new ResponseApiKeysDto();

        List<ApiKeyRowDto> apiKeyRowDtos = getApiKeyRowDtosFromApiKeyModel(apiKeyModels);

        responseApiKeysDto.setItems(apiKeyRowDtos);
        responseApiKeysDto.setTotal(apiKeyModels.size());

        return responseApiKeysDto;
    }

    private List<ApiKeyRowDto> getApiKeyRowDtosFromApiKeyModel(List<ApiKeyModel> apiKeyModels){
        List<ApiKeyRowDto> apiKeyRowDtos = new ArrayList<>();

        for(ApiKeyModel apiKeyModel : apiKeyModels){
            apiKeyRowDtos.add(getApiKeyRowDtoFromApiKeyModel(apiKeyModel));
        }

        return apiKeyRowDtos;
    }

    private ApiKeyRowDto getApiKeyRowDtoFromApiKeyModel(ApiKeyModel apiKeyModel){

        ApiKeyRowDto apiKeyRowDto = new ApiKeyRowDto();
        apiKeyRowDto.setId(apiKeyModel.getId());
        apiKeyRowDto.setName(apiKeyModel.getName());
        apiKeyRowDto.setPdnd(apiKeyModel.isPdnd());
        apiKeyRowDto.setGroups(apiKeyModel.getGroups());
        apiKeyRowDto.setStatus(ApiKeyStatusDto.fromValue(apiKeyModel.getStatus()));
        return apiKeyRowDto;
    }

}
