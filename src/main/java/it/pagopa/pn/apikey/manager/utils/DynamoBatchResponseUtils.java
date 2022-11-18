package it.pagopa.pn.apikey.manager.utils;

import it.pagopa.pn.apikey.manager.entity.PaAggregationModel;
import it.pagopa.pn.apikey.manager.model.PnBatchGetItemResponse;
import it.pagopa.pn.apikey.manager.model.PnBatchPutItemResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbAsyncTable;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedAsyncClient;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.BatchGetResultPage;
import software.amazon.awssdk.enhanced.dynamodb.model.BatchWriteResult;

@Component
@Slf4j
public class DynamoBatchResponseUtils {

    private final DynamoDbAsyncTable<PaAggregationModel> paAggregationsTable;

    public DynamoBatchResponseUtils(DynamoDbEnhancedAsyncClient dynamoDbEnhancedClient,
                                    @Value("${pn.apikey.manager.dynamodb.tablename.pa-aggregations}") String tableName) {
        this.paAggregationsTable = dynamoDbEnhancedClient.table(tableName, TableSchema.fromBean(PaAggregationModel.class));
    }

    public PnBatchGetItemResponse convertPaAggregationsBatchGetItemResponse(BatchGetResultPage batchGetResultPage){
        PnBatchGetItemResponse pnBatchGetItemResponse = new PnBatchGetItemResponse();
        pnBatchGetItemResponse.setFounded(batchGetResultPage.resultsForTable(paAggregationsTable));
        pnBatchGetItemResponse.setUnprocessed(batchGetResultPage.unprocessedKeysForTable(paAggregationsTable));
        log.info("BatchGetItemResponse founded: {}, unprocessed: {}", pnBatchGetItemResponse.getFounded(), pnBatchGetItemResponse.getUnprocessed());
        return pnBatchGetItemResponse;
    }

    public PnBatchPutItemResponse convertPaAggregationsBatchPutItemResponse(BatchWriteResult batchWriteResult){
        PnBatchPutItemResponse pnBatchPutItemResponse = new PnBatchPutItemResponse();
        pnBatchPutItemResponse.setUnprocessed(batchWriteResult.unprocessedPutItemsForTable(paAggregationsTable));
        log.info("BatchPutItemResponse unprocessed: {}", pnBatchPutItemResponse.getUnprocessed());
        return pnBatchPutItemResponse;
    }
}
