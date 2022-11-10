package it.pagopa.pn.apikey.manager.utils;

import it.pagopa.pn.apikey.manager.model.PnBatchGetItemResponse;
import it.pagopa.pn.apikey.manager.model.PnBatchPutItemResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbAsyncTable;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedAsyncClient;
import software.amazon.awssdk.enhanced.dynamodb.model.BatchGetResultPage;
import software.amazon.awssdk.enhanced.dynamodb.model.BatchWriteResult;
import software.amazon.awssdk.services.dynamodb.model.BatchGetItemResponse;

import java.util.HashMap;

import static org.mockito.ArgumentMatchers.any;

@ContextConfiguration(classes = {DynamoBatchResponseUtils.class})
@ExtendWith(SpringExtension.class)
class DynamoBatchResponseUtilsTest {

    @Autowired
    private DynamoBatchResponseUtils dynamoBatchResponseUtils;

    @MockBean
    private DynamoDbEnhancedAsyncClient dynamoDbEnhancedAsyncClient;

    @MockBean
    private DynamoDbAsyncTable<Object> dynamoDbAsyncTable;

    /**
     * Method under test: {@link DynamoBatchResponseUtils#convertPaAggregationsBatchGetItemResponse(BatchGetResultPage)}
     */
    @Test
    void testConvertPaAggregationsBatchGetItemResponse() {
        Mockito.when(dynamoDbEnhancedAsyncClient.table(any(), any())).thenReturn(dynamoDbAsyncTable);
        DynamoBatchResponseUtils dynamoBatchResponseUtils = new DynamoBatchResponseUtils(dynamoDbEnhancedAsyncClient, "");

        BatchGetResultPage batchGetResultPage = BatchGetResultPage.builder()
                .batchGetItemResponse(BatchGetItemResponse.builder()
                        .unprocessedKeys(new HashMap<>())
                        .responses(new HashMap<>()).build()).build();
        PnBatchGetItemResponse pnBatchGetItemResponse = dynamoBatchResponseUtils.convertPaAggregationsBatchGetItemResponse(batchGetResultPage);
        Assertions.assertEquals(0, pnBatchGetItemResponse.getFounded().size());
        Assertions.assertEquals(0, pnBatchGetItemResponse.getUnprocessed().size());

    }

    @Test
    void testConvertPaAggregationsBatchPutItemResponse() {
        Mockito.when(dynamoDbEnhancedAsyncClient.table(any(), any())).thenReturn(dynamoDbAsyncTable);
        DynamoBatchResponseUtils dynamoBatchResponseUtils = new DynamoBatchResponseUtils(dynamoDbEnhancedAsyncClient, "");

        BatchWriteResult batchWriteResult = BatchWriteResult.builder().unprocessedRequests(new HashMap<>()).build();
        PnBatchPutItemResponse pnBatchPutItemResponse = dynamoBatchResponseUtils.convertPaAggregationsBatchPutItemResponse(batchWriteResult);
        Assertions.assertEquals(0, pnBatchPutItemResponse.getUnprocessed().size());
    }
}

