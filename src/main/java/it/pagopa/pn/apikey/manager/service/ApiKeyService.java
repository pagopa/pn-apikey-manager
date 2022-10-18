package it.pagopa.pn.apikey.manager.service;

import it.pagopa.pn.apikey.manager.entity.ApiKeyHistory;
import it.pagopa.pn.apikey.manager.entity.ApiKeyModel;
import it.pagopa.pn.apikey.manager.generated.openapi.rest.v1.dto.*;
import it.pagopa.pn.apikey.manager.repository.ApiKeyRepository;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;


@Service
@Slf4j
public class ApiKeyService {

    private final ApiKeyRepository apiKeyRepository;

    public ApiKeyService(ApiKeyRepository apiKeyRepository) {
        this.apiKeyRepository = apiKeyRepository;
    }

    public Mono<ApiKeysResponseDto> getApiKeyList(String xPagopaPnCxId, List<String> xPagopaPnCxGroups) {
        return apiKeyRepository.getAllWithFilter(xPagopaPnCxId,xPagopaPnCxGroups)
                .map(this::convertResponsetoDto);
    }

    private ApiKeysResponseDto convertResponsetoDto(List<ApiKeyModel> apiKeyModels){
        ApiKeysResponseDto apiKeysResponseDto = new ApiKeysResponseDto();

        List<ApiKeyRowDto> apiKeyRowDtos = getApiKeyRowDtosFromApiKeyModel(apiKeyModels);

        apiKeysResponseDto.setItems(apiKeyRowDtos);

        return apiKeysResponseDto;
    }

    @SneakyThrows
    private List<ApiKeyRowDto> getApiKeyRowDtosFromApiKeyModel(List<ApiKeyModel> apiKeyModels){
        List<ApiKeyRowDto> apiKeyRowDtos = new ArrayList<>();

        for(ApiKeyModel apiKeyModel : apiKeyModels){
            apiKeyRowDtos.add(getApiKeyRowDtoFromApiKeyModel(apiKeyModel));
        }

        return apiKeyRowDtos;
    }

    @SneakyThrows
    private ApiKeyRowDto getApiKeyRowDtoFromApiKeyModel(ApiKeyModel apiKeyModel){
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");

        ApiKeyRowDto apiKeyRowDto = new ApiKeyRowDto();
        apiKeyRowDto.setId(apiKeyModel.getId());
        apiKeyRowDto.setName(apiKeyModel.getName());
        apiKeyRowDto.setValue(apiKeyModel.getVirtualKey());
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
