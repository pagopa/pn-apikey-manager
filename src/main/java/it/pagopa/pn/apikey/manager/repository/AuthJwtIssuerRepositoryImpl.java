package it.pagopa.pn.apikey.manager.repository;

import it.pagopa.pn.apikey.manager.entity.AuthJwtIssuerModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbAsyncTable;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedAsyncClient;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;

@lombok.CustomLog
@Component
public class AuthJwtIssuerRepositoryImpl implements AuthJwtIssuerRepository {
    private final DynamoDbAsyncTable<AuthJwtIssuerModel> table;

    public AuthJwtIssuerRepositoryImpl(DynamoDbEnhancedAsyncClient dynamoDbEnhancedClient,
                                       @Value("${pn.apikey.manager.dao.jwtauthissuertablename}") String tableName) {
        this.table = dynamoDbEnhancedClient.table(tableName, TableSchema.fromBean(AuthJwtIssuerModel.class));
    }

    @Override
    public Mono<AuthJwtIssuerModel> save(AuthJwtIssuerModel authJwtIssuerModel) {
        log.debug("Inserting data {} in DynamoDB table {}",authJwtIssuerModel,table);
        return Mono.fromFuture(table.putItem(authJwtIssuerModel))
                .doOnNext(unused -> log.info("Inserted data in DynamoDB table {}",table))
                .thenReturn(authJwtIssuerModel);
    }
}
