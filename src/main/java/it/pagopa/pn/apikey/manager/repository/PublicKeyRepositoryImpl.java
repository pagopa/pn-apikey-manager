package it.pagopa.pn.apikey.manager.repository;

import it.pagopa.pn.apikey.manager.config.PnApikeyManagerConfig;
import it.pagopa.pn.apikey.manager.entity.PublicKeyModel;
import it.pagopa.pn.apikey.manager.exception.ApiKeyManagerException;
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
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import static it.pagopa.pn.apikey.manager.exception.ApiKeyManagerExceptionError.PUBLIC_KEY_DOES_NOT_EXISTS;
import static it.pagopa.pn.apikey.manager.utils.QueryUtils.expressionBuilder;

@lombok.CustomLog
@Component
public class PublicKeyRepositoryImpl implements PublicKeyRepository {
    private final DynamoDbAsyncTable<PublicKeyModel> table;

    public PublicKeyRepositoryImpl(DynamoDbEnhancedAsyncClient dynamoDbEnhancedClient,
                                   PnApikeyManagerConfig pnApikeyManagerConfig) {
        this.table = dynamoDbEnhancedClient.table(pnApikeyManagerConfig.getDao().getPublicKeyTableName(), TableSchema.fromBean(PublicKeyModel.class));
    }

    @Override
    public Mono<PublicKeyModel> updateItemStatus(PublicKeyModel publicKeyModel, List<String> invalidStartedStatus) {
        return Mono.fromFuture(table.updateItem(createUpdateItemEnhancedRequest(publicKeyModel, invalidStartedStatus)));
    }

    @Override
    public Mono<PublicKeyModel> findByKidAndCxId(String kid, String cxId) {
        Key key = Key.builder()
                .partitionValue(kid)
                .sortValue(cxId)
                .build();
        return Mono.fromFuture(table.getItem(key))
                .switchIfEmpty(Mono.error(new ApiKeyManagerException(PUBLIC_KEY_DOES_NOT_EXISTS, HttpStatus.NOT_FOUND)));

    }

    private UpdateItemEnhancedRequest<PublicKeyModel> createUpdateItemEnhancedRequest(PublicKeyModel publicKeyModel, List<String> invalidStartedStatus) {
        Map<String, String> expressionNames = new HashMap<>();
        expressionNames.put("#status", "status");

        Map<String, AttributeValue> expressionValues = new HashMap<>();
        StringBuilder expressionBuilder = new StringBuilder();
        IntStream.range(0, invalidStartedStatus.size()).forEach(idx -> {
            expressionValues.put(":" + invalidStartedStatus.get(idx), AttributeValue.builder().s(invalidStartedStatus.get(idx)).build());
            if(idx == invalidStartedStatus.size() - 1) {
                expressionBuilder.append("#status <> :").append(invalidStartedStatus.get(idx));
            } else {
                expressionBuilder.append("#status <> :").append(invalidStartedStatus.get(idx)).append(" AND ");
            }
        });

        return UpdateItemEnhancedRequest
                .builder(PublicKeyModel.class)
                .conditionExpression(expressionBuilder(expressionBuilder.toString(), expressionValues, expressionNames))
                .item(publicKeyModel)
                .ignoreNulls(true)
                .build();
    }

    @Override
    public Mono<PublicKeyModel> save(PublicKeyModel publicKeyModel) {
        log.debug("Inserting data {} in DynamoDB table {}",publicKeyModel,table);
        return Mono.fromFuture(table.putItem(publicKeyModel))
                .doOnNext(unused -> log.info("Inserted data in DynamoDB table {}",table))
                .thenReturn(publicKeyModel);
    }
}
