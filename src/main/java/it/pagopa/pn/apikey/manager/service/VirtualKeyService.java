package it.pagopa.pn.apikey.manager.service;

import it.pagopa.pn.apikey.manager.client.PnDataVaultClient;
import it.pagopa.pn.apikey.manager.constant.ApiKeyConstant;
import it.pagopa.pn.apikey.manager.converter.VirtualKeyConverter;
import it.pagopa.pn.apikey.manager.entity.ApiKeyModel;
import it.pagopa.pn.apikey.manager.exception.ApiKeyManagerException;
import it.pagopa.pn.apikey.manager.generated.openapi.msclient.pndatavault.v1.dto.BaseRecipientDtoDto;
import it.pagopa.pn.apikey.manager.generated.openapi.server.v1.dto.CxTypeAuthFleetDto;
import it.pagopa.pn.apikey.manager.generated.openapi.server.v1.dto.VirtualKeysResponseDto;
import it.pagopa.pn.apikey.manager.repository.ApiKeyPageable;
import it.pagopa.pn.apikey.manager.repository.ApiKeyRepository;
import it.pagopa.pn.apikey.manager.utils.VirtualKeyUtils;
import lombok.CustomLog;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.enhanced.dynamodb.model.Page;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static it.pagopa.pn.apikey.manager.exception.ApiKeyManagerExceptionError.APIKEY_CX_TYPE_NOT_ALLOWED;

@Service
@RequiredArgsConstructor
@CustomLog
@Slf4j
public class VirtualKeyService {

    private final ApiKeyRepository apiKeyRepository;
    private final VirtualKeyConverter virtualKeyConverter;
    private final PnDataVaultClient pnDataVaultClient;

    public Mono<VirtualKeysResponseDto> getVirtualKeys(String xPagopaPnUid, CxTypeAuthFleetDto xPagopaPnCxType, String xPagopaPnCxId, List<String> xPagopaPnCxGroups, String xPagopaPnCxRole,
                                                       Integer limit, String lastKey, String lastUpdate, Boolean showVirtualKey) {
        if (!ApiKeyConstant.ALLOWED_CX_TYPE_VIRTUALKEY.contains(xPagopaPnCxType)) {
            log.error("CxTypeAuthFleet {} not allowed", xPagopaPnCxType);
            return Mono.error(new ApiKeyManagerException(String.format(APIKEY_CX_TYPE_NOT_ALLOWED, xPagopaPnCxType), HttpStatus.FORBIDDEN));
        }
        ApiKeyPageable pageable = toApiKeyPageable(limit, lastKey, lastUpdate);
        return VirtualKeyUtils.isRoleAdmin(xPagopaPnCxRole, xPagopaPnCxGroups)
                .flatMap(admin -> {
                    log.debug("admin: {}", admin);
                    Mono<Page<ApiKeyModel>> page = apiKeyRepository.getVirtualKeys(xPagopaPnUid, xPagopaPnCxId, new ArrayList<>(), pageable, admin);
                    if (admin) {
                        return page.flatMap(apiKeyModelPage -> {
                            List<String> internalIds = apiKeyModelPage.items().stream()
                                    .map(ApiKeyModel::getUid)
                                    .collect(Collectors.toList());
                            return pnDataVaultClient.getRecipientDenominationByInternalId(internalIds)
                                    .collectMap(BaseRecipientDtoDto::getInternalId, baseRecipientDtoDto -> baseRecipientDtoDto)
                                    .flatMap(mapBaseRecipient -> convertToDtoAndSetTotal(xPagopaPnUid, xPagopaPnCxId, showVirtualKey, apiKeyModelPage, mapBaseRecipient, admin));
                        });
                    } else {
                        return page.flatMap(apiKeyModelPage -> convertToDtoAndSetTotal(xPagopaPnUid, xPagopaPnCxId, showVirtualKey, apiKeyModelPage, null, admin));
                    }
                });
    }

    private Mono<VirtualKeysResponseDto> convertToDtoAndSetTotal(String xPagopaPnUid, String xPagopaPnCxId, Boolean showVirtualKey, Page<ApiKeyModel> apiKeyModelPage,
                                                                 Map<String, BaseRecipientDtoDto> mapBaseRecipient, Boolean admin) {
        VirtualKeysResponseDto virtualKeysResponseDto = virtualKeyConverter.convertResponseToDto(apiKeyModelPage, mapBaseRecipient, showVirtualKey);
        return apiKeyRepository.countWithFilters(xPagopaPnUid, xPagopaPnCxId, admin).map(integer -> {
            virtualKeysResponseDto.setTotal(integer);
            return virtualKeysResponseDto;
        });
    }

    private ApiKeyPageable toApiKeyPageable(Integer limit, String lastKey, String lastUpdate) {
        return ApiKeyPageable.builder()
                .limit(limit)
                .lastEvaluatedKey(lastKey)
                .lastEvaluatedLastUpdate(lastUpdate)
                .build();
    }
}
