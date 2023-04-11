package it.pagopa.pn.apikey.manager.utils;

import it.pagopa.pn.apikey.manager.constant.ApiKeyConstant;
import it.pagopa.pn.apikey.manager.entity.ApiKeyModel;
import it.pagopa.pn.apikey.manager.repository.ApiKeyPageable;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.enhanced.dynamodb.Expression;
import software.amazon.awssdk.enhanced.dynamodb.model.Page;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

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

}