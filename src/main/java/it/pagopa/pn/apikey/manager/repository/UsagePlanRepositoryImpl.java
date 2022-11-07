package it.pagopa.pn.apikey.manager.repository;

import it.pagopa.pn.apikey.manager.entity.UsagePlanModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbAsyncTable;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedAsyncClient;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;

@Slf4j
@Component
public class UsagePlanRepositoryImpl implements UsagePlanRepository {

    private final DynamoDbAsyncTable<UsagePlanModel> table;

    public UsagePlanRepositoryImpl(DynamoDbEnhancedAsyncClient dynamoDbEnhancedClient,
                                   @Value("${pn.apikey.manager.dynamodb.tablename.usagePlan}") String tableName) {
        this.table = dynamoDbEnhancedClient.table(tableName, TableSchema.fromBean(UsagePlanModel.class));
    }

}
