package it.pagopa.pn.apikey.manager.repository;

import it.pagopa.pn.apikey.manager.config.PnApikeyManagerConfig;
import it.pagopa.pn.apikey.manager.entity.PublicKeyModel;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.enhanced.dynamodb.*;
import software.amazon.awssdk.enhanced.dynamodb.model.Page;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryEnhancedRequest;

import java.util.HashMap;
import java.util.Map;

@lombok.CustomLog
@Component
public class PublicKeyRepositoryImpl implements PublicKeyRepository {
    private final DynamoDbAsyncTable<PublicKeyModel> table;

    public PublicKeyRepositoryImpl(DynamoDbEnhancedAsyncClient dynamoDbEnhancedClient,
                                   PnApikeyManagerConfig pnApikeyManagerConfig) {
        this.table = dynamoDbEnhancedClient.table(pnApikeyManagerConfig.getDao().getPublicKeyTableName(), TableSchema.fromBean(PublicKeyModel.class));
    }

    @Override
    public Mono<Page<PublicKeyModel>> findByCxIdAndWithoutTtl(String xPagopaPnCxId) {

        QueryConditional queryConditional = QueryConditional
                .keyEqualTo(Key.builder().partitionValue(xPagopaPnCxId)
                        .build());

        Map<String, String> expressionNames = new HashMap<>();
        expressionNames.put("#ttl", "ttl");

        QueryEnhancedRequest queryEnhancedRequest = QueryEnhancedRequest.builder()
                .queryConditional(queryConditional)
                .filterExpression(Expression.builder().expression("attribute_not_exists(#ttl)").expressionNames(expressionNames).build())
                .scanIndexForward(false)
                .build();

        return Mono.from(table.index(PublicKeyModel.GSI_CXID_STATUS)
                .query(queryEnhancedRequest));
    }

}
