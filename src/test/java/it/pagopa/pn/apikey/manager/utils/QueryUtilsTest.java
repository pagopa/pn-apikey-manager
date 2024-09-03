package it.pagopa.pn.apikey.manager.utils;

import it.pagopa.pn.apikey.manager.constant.ApiKeyConstant;
import it.pagopa.pn.apikey.manager.constant.PublicKeyConstant;
import it.pagopa.pn.apikey.manager.entity.ApiKeyModel;
import it.pagopa.pn.apikey.manager.entity.PublicKeyModel;
import it.pagopa.pn.apikey.manager.repository.ApiKeyPageable;
import it.pagopa.pn.apikey.manager.repository.PublicKeyPageable;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.enhanced.dynamodb.Expression;
import software.amazon.awssdk.enhanced.dynamodb.model.Page;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class QueryUtilsTest {

    /**
     * Method under test: {@link QueryUtils#expressionBuilder(String, Map, Map)}
     */
    @Test
    void testExpressionBuilder() {
        HashMap<String, AttributeValue> expressionValues = new HashMap<>();
        Expression actualExpressionBuilderResult = QueryUtils.expressionBuilder("Expression", expressionValues,
                new HashMap<>());
        assertEquals("Expression", actualExpressionBuilderResult.expression());
        Map<String, AttributeValue> expressionValuesResult = actualExpressionBuilderResult.expressionValues();
        assertTrue(expressionValuesResult.isEmpty());
    }

    /**
     * Method under test: {@link QueryUtils#expressionBuilder(String, Map, Map)}
     */
    @Test
    void testExpressionBuilder2() {
        Expression actualExpressionBuilderResult = QueryUtils.expressionBuilder(null, null, null);
        assertNull(actualExpressionBuilderResult.expression());
        assertNull(actualExpressionBuilderResult.expressionValues());
        assertNull(actualExpressionBuilderResult.expressionNames());
    }

    @Test
    void testGetNewPageable() {
        Map<String, AttributeValue> attributeValues = new HashMap<>();
        attributeValues.put(ApiKeyConstant.PK, AttributeValue.builder().s("id").build());
        attributeValues.put(ApiKeyConstant.LAST_UPDATE, AttributeValue.builder().s("lastUpdate").build());
        Page<ApiKeyModel> page = Page.create(new ArrayList<>(), attributeValues);
        ApiKeyPageable oldPageable = ApiKeyPageable.builder()
                .lastEvaluatedKey("oldKey")
                .lastEvaluatedLastUpdate("oldLastUpdate")
                .limit(1)
                .build();
        ApiKeyPageable newPageable = QueryUtils.getNewPageable(page, oldPageable);
        assertEquals("id", newPageable.getLastEvaluatedKey());
        assertEquals("lastUpdate", newPageable.getLastEvaluatedLastUpdate());
        assertEquals(oldPageable.getLimit(), newPageable.getLimit());
    }

    @Test
    void getNewPageable_returnsCorrectPageable() {
        Map<String, AttributeValue> attributeValues = new HashMap<>();
        attributeValues.put(PublicKeyConstant.KID, AttributeValue.builder().s("kid1").build());
        attributeValues.put(PublicKeyConstant.CREATED_AT, AttributeValue.builder().s("2023-10-01T00:00:00Z").build());
        Page<PublicKeyModel> page = Page.create(new ArrayList<>(), attributeValues);
        PublicKeyPageable oldPageable = PublicKeyPageable.builder()
                .lastEvaluatedKey("oldKey")
                .createdAt("oldCreatedAt")
                .limit(1)
                .build();
        PublicKeyPageable newPageable = QueryUtils.getNewPageable(page, oldPageable);
        assertEquals("kid1", newPageable.getLastEvaluatedKey());
        assertEquals("2023-10-01T00:00:00Z", newPageable.getCreatedAt());
        assertEquals(oldPageable.getLimit(), newPageable.getLimit());
    }

    @Test
    void testAdjustPageResult() {
        ApiKeyPageable pageable = ApiKeyPageable.builder()
                .lastEvaluatedKey("key")
                .lastEvaluatedLastUpdate("lastUpdate")
                .limit(1)
                .build();

        Map<String, AttributeValue> attributeValues = new HashMap<>();
        attributeValues.put(ApiKeyConstant.PK, AttributeValue.builder().s("1").build());
        attributeValues.put(ApiKeyConstant.LAST_UPDATE, AttributeValue.builder().s("2").build());

        ApiKeyModel apiKey = new ApiKeyModel();
        apiKey.setId("newId");
        apiKey.setLastUpdate(LocalDateTime.MIN);

        List<ApiKeyModel> result = QueryUtils.adjustPageResult(List.of(apiKey, new ApiKeyModel()), pageable, attributeValues);
        assertEquals(1, result.size());
        assertEquals("newId", attributeValues.get(ApiKeyConstant.PK).s());
        assertEquals("-999999999-01-01T00:00:00", attributeValues.get(ApiKeyConstant.LAST_UPDATE).s());
    }

    @Test
    void adjustPageResult_truncatesResultWhenLimitExceeded() {
        PublicKeyPageable pageable = PublicKeyPageable.builder()
                .limit(1)
                .build();

        Map<String, AttributeValue> attributeValues = new HashMap<>();
        attributeValues.put(PublicKeyConstant.KID, AttributeValue.builder().s("1").build());
        attributeValues.put(PublicKeyConstant.CREATED_AT, AttributeValue.builder().s("2").build());

        Instant now = Instant.now();
        PublicKeyModel publicKey1 = new PublicKeyModel();
        publicKey1.setKid("kid1");
        publicKey1.setCreatedAt(now);

        PublicKeyModel publicKey2 = new PublicKeyModel();
        publicKey2.setKid("kid2");
        publicKey2.setCreatedAt(Instant.now().plus(1, java.time.temporal.ChronoUnit.DAYS));

        List<PublicKeyModel> result = QueryUtils.adjustPageResult(List.of(publicKey1, publicKey2), pageable, attributeValues);
        assertEquals(1, result.size());
        assertEquals("kid1", attributeValues.get(PublicKeyConstant.KID).s());
        assertEquals(now.toString(), attributeValues.get(PublicKeyConstant.CREATED_AT).s());
    }

    @Test
    void adjustPageResult_doesNotTruncateWhenLimitNotExceeded() {
        PublicKeyPageable pageable = PublicKeyPageable.builder()
                .limit(2)
                .build();

        Map<String, AttributeValue> attributeValues = new HashMap<>();
        attributeValues.put(PublicKeyConstant.KID, AttributeValue.builder().s("1").build());
        attributeValues.put(PublicKeyConstant.CREATED_AT, AttributeValue.builder().s("2").build());

        PublicKeyModel publicKey1 = new PublicKeyModel();
        publicKey1.setKid("kid1");
        publicKey1.setCreatedAt(Instant.now());

        List<PublicKeyModel> result = QueryUtils.adjustPageResult(List.of(publicKey1), pageable, attributeValues);
        assertEquals(1, result.size());
        assertEquals("1", attributeValues.get(PublicKeyConstant.KID).s());
        assertEquals("2", attributeValues.get(PublicKeyConstant.CREATED_AT).s());
    }

    @Test
    void adjustPageResult_handlesEmptyResult() {
        PublicKeyPageable pageable = PublicKeyPageable.builder()
                .limit(1)
                .build();

        Map<String, AttributeValue> attributeValues = new HashMap<>();
        attributeValues.put(PublicKeyConstant.KID, AttributeValue.builder().s("1").build());
        attributeValues.put(PublicKeyConstant.CREATED_AT, AttributeValue.builder().s("2").build());

        List<PublicKeyModel> result = QueryUtils.adjustPageResult(List.of(), pageable, attributeValues);
        assertTrue(result.isEmpty());
        assertEquals("1", attributeValues.get(PublicKeyConstant.KID).s());
        assertEquals("2", attributeValues.get(PublicKeyConstant.CREATED_AT).s());
    }

    @Test
    void adjustPageResult_handlesNullLastEvaluatedKey() {
        PublicKeyPageable pageable = PublicKeyPageable.builder()
                .limit(1)
                .build();

        PublicKeyModel publicKey1 = new PublicKeyModel();
        publicKey1.setKid("kid1");
        publicKey1.setCreatedAt(Instant.now());

        List<PublicKeyModel> result = QueryUtils.adjustPageResult(List.of(publicKey1), pageable, null);
        assertEquals(1, result.size());
    }

}