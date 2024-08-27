package it.pagopa.pn.apikey.manager.repository;

import it.pagopa.pn.apikey.manager.entity.PublicKeyModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbAsyncTable;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedAsyncClient;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.Page;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryEnhancedRequest;

@lombok.CustomLog
@Component
public class PublicKeyRepositoryImpl implements PublicKeyRepository {
    private final DynamoDbAsyncTable<PublicKeyModel> table;

    public PublicKeyRepositoryImpl(DynamoDbEnhancedAsyncClient dynamoDbEnhancedClient,
                                   @Value("${pn.apikey.manager.dao.publickeytablename}") String tableName) {
        this.table = dynamoDbEnhancedClient.table(tableName, TableSchema.fromBean(PublicKeyModel.class));
    }

    @Override
    public Flux<PublicKeyModel> findByCxIdAndStatus(String xPagopaPnCxId, String status) {
        Key key = Key.builder().partitionValue(xPagopaPnCxId).sortValue(status).build();
        QueryConditional conditional = QueryConditional.keyEqualTo(key);

        QueryEnhancedRequest.Builder qeRequest = QueryEnhancedRequest
                .builder()
                .queryConditional(conditional);

        return Flux.from(this.table.index(PublicKeyModel.GSI_CXID_STATUS).query(qeRequest.build()).flatMapIterable(Page::items));
    }

    @Override
    public Mono<PublicKeyModel> save(PublicKeyModel publicKeyModel) {
        log.debug("Inserting data {} in DynamoDB table {}",publicKeyModel,table);
        return Mono.fromFuture(table.putItem(publicKeyModel))
                .doOnNext(unused -> log.info("Inserted data in DynamoDB table {}",table))
                .thenReturn(publicKeyModel);
    }
}
