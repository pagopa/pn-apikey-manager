package it.pagopa.pn.apikey.manager.repository;

import it.pagopa.pn.apikey.manager.entity.PublicKeyModel;
import it.pagopa.pn.apikey.manager.exception.ApiKeyManagerException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbAsyncTable;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedAsyncClient;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.UpdateItemEnhancedRequest;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.util.HashMap;
import java.util.Map;

import static it.pagopa.pn.apikey.manager.exception.ApiKeyManagerExceptionError.APIKEY_DOES_NOT_EXISTS;
import static it.pagopa.pn.apikey.manager.utils.QueryUtils.expressionBuilder;

@lombok.CustomLog
@Component
public class PublicKeyRepositoryImpl implements PublicKeyRepository {
    private final DynamoDbAsyncTable<PublicKeyModel> table;
    private static final int MIN_LIMIT = 100;
    private static final int EXPRESSION_GROUP = 2;

    public PublicKeyRepositoryImpl(DynamoDbEnhancedAsyncClient dynamoDbEnhancedClient,
                                   @Value("${pn.apikey.manager.dynamodb.tablename.publickey}") String tableName) {
        this.table = dynamoDbEnhancedClient.table(tableName, TableSchema.fromBean(PublicKeyModel.class));
    }

    @Override
    public Mono<PublicKeyModel> changeStatus(PublicKeyModel publicKeyModel) {
        return Mono.fromFuture(table.updateItem(updateItemEnhancedRequest(publicKeyModel)));
    }

    @Override
    public Mono<PublicKeyModel> findByKidAndCxId(String kid, String xPagopaPnCxId) {
        Key key = Key.builder()
                .partitionValue(kid)
                .sortValue(xPagopaPnCxId)
                .build();
        return Mono.fromFuture(table.getItem(key))
                .switchIfEmpty(Mono.error(new ApiKeyManagerException(APIKEY_DOES_NOT_EXISTS, HttpStatus.NOT_FOUND)));

    }

    private UpdateItemEnhancedRequest<PublicKeyModel> updateItemEnhancedRequest(PublicKeyModel publicKeyModel) {
        Map<String, String> expressionNames = new HashMap<>();
        expressionNames.put("#kid", "kid");
        expressionNames.put("#cxId", "cxId");

        Map<String, AttributeValue> expressionValues = new HashMap<>();
        expressionValues.put(":kid", AttributeValue.builder().s(publicKeyModel.getKid()).build());
        expressionValues.put(":cxId", AttributeValue.builder().s(publicKeyModel.getCxId()).build());

        return UpdateItemEnhancedRequest
                .builder(PublicKeyModel.class)
                .conditionExpression(expressionBuilder("#kid = :kid AND #cxId = :cxId", expressionValues, expressionNames))
                .item(publicKeyModel)
                .ignoreNulls(true)
                .build();
    }

}
