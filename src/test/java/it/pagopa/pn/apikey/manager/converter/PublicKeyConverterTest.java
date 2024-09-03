package it.pagopa.pn.apikey.manager.converter;

import static org.junit.jupiter.api.Assertions.*;

import it.pagopa.pn.apikey.manager.constant.PublicKeyConstant;
import it.pagopa.pn.apikey.manager.entity.PublicKeyModel;
import it.pagopa.pn.apikey.manager.generated.openapi.server.v1.dto.PublicKeysResponseDto;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.enhanced.dynamodb.model.Page;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.time.Instant;
import java.util.*;

class PublicKeyConverterTest {

    @Test
    void convertResponseToDto_returnsCorrectDtoWithPublicKey() {
        PublicKeyModel publicKeyModel = new PublicKeyModel();
        publicKeyModel.setKid("kid1");
        publicKeyModel.setName("name1");
        publicKeyModel.setPublicKey("publicKey1");
        publicKeyModel.setCreatedAt(new Date().toInstant());
        publicKeyModel.setStatus("ACTIVE");


        List<PublicKeyModel.StatusHistoryItem> statusHistory = new ArrayList<>();
        PublicKeyModel.StatusHistoryItem statusHistoryItem = new PublicKeyModel.StatusHistoryItem();
        statusHistoryItem.setStatus("ACTIVE");
        statusHistoryItem.setDate(Instant.now());
        statusHistory.add(statusHistoryItem);
        publicKeyModel.setStatusHistory(statusHistory);

        Page<PublicKeyModel> page = Page.create(Collections.singletonList(publicKeyModel), null);

        PublicKeyConverter converter = new PublicKeyConverter();
        PublicKeysResponseDto responseDto = converter.convertResponseToDto(page, true);

        assertEquals(1, responseDto.getItems().size());
        assertEquals("kid1", responseDto.getItems().get(0).getKid());
        assertEquals("publicKey1", responseDto.getItems().get(0).getValue());
    }

    @Test
    void convertResponseToDto_returnsCorrectDtoWithoutPublicKey() {
        PublicKeyModel publicKeyModel = new PublicKeyModel();
        publicKeyModel.setKid("kid1");
        publicKeyModel.setName("name1");
        publicKeyModel.setPublicKey("publicKey1");
        publicKeyModel.setCreatedAt(new Date().toInstant());
        publicKeyModel.setStatus("ACTIVE");

        Page<PublicKeyModel> page = Page.create(Collections.singletonList(publicKeyModel), null);

        PublicKeyConverter converter = new PublicKeyConverter();
        PublicKeysResponseDto responseDto = converter.convertResponseToDto(page, false);

        assertEquals(1, responseDto.getItems().size());
        assertEquals("kid1", responseDto.getItems().get(0).getKid());
        assertNull(responseDto.getItems().get(0).getValue());
    }

    @Test
    void convertResponseToDto_handlesEmptyPage() {
        Page<PublicKeyModel> page = Page.create(Collections.emptyList(), null);

        PublicKeyConverter converter = new PublicKeyConverter();
        PublicKeysResponseDto responseDto = converter.convertResponseToDto(page, true);

        assertTrue(responseDto.getItems().isEmpty());
    }

    @Test
    void convertResponseToDto_handlesLastEvaluatedKey() {
        PublicKeyModel publicKeyModel = new PublicKeyModel();
        publicKeyModel.setKid("kid1");
        publicKeyModel.setName("name1");
        publicKeyModel.setPublicKey("publicKey1");
        publicKeyModel.setCreatedAt(new Date().toInstant());
        publicKeyModel.setStatus("ACTIVE");

        Map<String, AttributeValue> lastEvaluatedKey = new HashMap<>();
        lastEvaluatedKey.put(PublicKeyConstant.KID, AttributeValue.builder().s("lastKid").build());
        lastEvaluatedKey.put(PublicKeyConstant.CREATED_AT, AttributeValue.builder().s("2023-10-01T00:00:00Z").build());

        Page<PublicKeyModel> page = Page.create(Collections.singletonList(publicKeyModel), lastEvaluatedKey);

        PublicKeyConverter converter = new PublicKeyConverter();
        PublicKeysResponseDto responseDto = converter.convertResponseToDto(page, true);

        assertEquals("lastKid", responseDto.getLastKey());
        assertEquals("2023-10-01T00:00:00Z", responseDto.getCreatedAt());
    }
}