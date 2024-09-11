package it.pagopa.pn.apikey.manager.converter;

import it.pagopa.pn.apikey.manager.entity.PublicKeyModel;
import it.pagopa.pn.apikey.manager.middleware.queue.consumer.event.PublicKeyEvent;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import it.pagopa.pn.apikey.manager.constant.PublicKeyConstant;
import it.pagopa.pn.apikey.manager.entity.PublicKeyModel;
import it.pagopa.pn.apikey.manager.generated.openapi.server.v1.dto.PublicKeyRowDto;
import it.pagopa.pn.apikey.manager.generated.openapi.server.v1.dto.PublicKeyStatusDto;
import it.pagopa.pn.apikey.manager.generated.openapi.server.v1.dto.PublicKeyStatusHistoryDto;
import it.pagopa.pn.apikey.manager.generated.openapi.server.v1.dto.PublicKeysResponseDto;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.enhanced.dynamodb.model.Page;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.util.Date.from;

@Component
public class PublicKeyConverter {

    public Mono<PublicKeyModel> convertPayloadToModel(PublicKeyEvent.Payload payload) {
        PublicKeyModel model = new PublicKeyModel();
        model.setKid(payload.getKid());
        model.setCxId(payload.getCxId());
        return Mono.just(model);
    }

    public PublicKeysResponseDto convertResponseToDto(Page<PublicKeyModel> pagePublicKeyModels, Boolean showPublicKey) {
        List<PublicKeyModel> publicKeyModels = pagePublicKeyModels.items();

        PublicKeysResponseDto publicKeysResponseDto = new PublicKeysResponseDto();

        List<PublicKeyRowDto> publicKeyRowDtos = getPublicKeyRowDtosFromPublicKeyModel(publicKeyModels, showPublicKey);

        publicKeysResponseDto.setItems(publicKeyRowDtos);

        if (pagePublicKeyModels.lastEvaluatedKey() != null) {
            Map<String, AttributeValue> lastKey = pagePublicKeyModels.lastEvaluatedKey();
            publicKeysResponseDto.setLastKey(lastKey.get(PublicKeyConstant.KID).s());
            publicKeysResponseDto.setCreatedAt(lastKey.get(PublicKeyConstant.CREATED_AT).s());
        }

        return publicKeysResponseDto;
    }

    private List<PublicKeyRowDto> getPublicKeyRowDtosFromPublicKeyModel(List<PublicKeyModel> publicKeyModels, Boolean showPublicKey) {
        List<PublicKeyRowDto> publicKeyRowDtos = new ArrayList<>();

        for (PublicKeyModel publicKeyModel : publicKeyModels) {
            publicKeyRowDtos.add(getPublicKeyRowDtoFromPublicKeyModel(publicKeyModel, showPublicKey));
        }

        return publicKeyRowDtos;
    }

    private PublicKeyRowDto getPublicKeyRowDtoFromPublicKeyModel(PublicKeyModel publicKeyModel, Boolean showPublicKey) {
        PublicKeyRowDto publicKeyRowDto = new PublicKeyRowDto();
        publicKeyRowDto.setKid(publicKeyModel.getKid());
        publicKeyRowDto.setIssuer(publicKeyModel.getIssuer());
        publicKeyRowDto.setName(publicKeyModel.getName());
        if (Boolean.TRUE.equals(showPublicKey)) {
            publicKeyRowDto.setValue(publicKeyModel.getPublicKey());
        }
        publicKeyRowDto.setCreatedAt(from(publicKeyModel.getCreatedAt()));
        publicKeyRowDto.setStatus(PublicKeyStatusDto.fromValue(publicKeyModel.getStatus()));
        for (PublicKeyModel.StatusHistoryItem statusHistoryItem : publicKeyModel.getStatusHistory()) {
            publicKeyRowDto.addStatusHistoryItem(getPublicKeyStatusHistoryDtoFromPublicKeyHistory(statusHistoryItem));
        }
        return publicKeyRowDto;
    }

    private PublicKeyStatusHistoryDto getPublicKeyStatusHistoryDtoFromPublicKeyHistory(PublicKeyModel.StatusHistoryItem statusHistoryItem) {
        PublicKeyStatusHistoryDto publicKeyStatusHistoryDto = new PublicKeyStatusHistoryDto();
        publicKeyStatusHistoryDto.setStatus(PublicKeyStatusDto.fromValue(statusHistoryItem.getStatus()));

        publicKeyStatusHistoryDto.setDate(from(statusHistoryItem.getDate()));
        publicKeyStatusHistoryDto.setChangedByDenomination(statusHistoryItem.getChangeByDenomination());

        return publicKeyStatusHistoryDto;
    }
}
