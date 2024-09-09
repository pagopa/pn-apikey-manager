package it.pagopa.pn.apikey.manager.repository;

import it.pagopa.pn.apikey.manager.config.PnApikeyManagerConfig;
import it.pagopa.pn.apikey.manager.entity.PublicKeyModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.reactivestreams.Subscriber;
import reactor.test.StepVerifier;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbAsyncIndex;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbAsyncTable;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedAsyncClient;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryEnhancedRequest;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class PublicKeyRepositoryImplTest {

    private PublicKeyRepositoryImpl repository;
    private DynamoDbAsyncTable<PublicKeyModel> table;

    @BeforeEach
    void setUp() {
        DynamoDbEnhancedAsyncClient dynamoDbEnhancedClient = mock(DynamoDbEnhancedAsyncClient.class);
        table = mock(DynamoDbAsyncTable.class);
        when(dynamoDbEnhancedClient.table(anyString(), any(TableSchema.class))).thenReturn(table);
        PnApikeyManagerConfig pnApikeyManagerConfig = new PnApikeyManagerConfig();
        PnApikeyManagerConfig.Dao dao = new PnApikeyManagerConfig.Dao();
        dao.setPublicKeyTableName("pn-publicKey");
        pnApikeyManagerConfig.setDao(dao);

        repository = new PublicKeyRepositoryImpl(dynamoDbEnhancedClient, pnApikeyManagerConfig);
    }

    @Test
    void findByCxIdAndWithoutTtl_withValidCxId_returnsMonoOfPage() {
        PublicKeyModel publicKeyModel = new PublicKeyModel();
        publicKeyModel.setKid("kid");
        publicKeyModel.setCxId("cxId");
        DynamoDbAsyncIndex<PublicKeyModel> index = mock(DynamoDbAsyncIndex.class);
        when(table.index(any())).thenReturn(index);
        when(index.query((QueryEnhancedRequest) any())).thenReturn(Subscriber::onComplete);

        StepVerifier.create(repository.findByCxIdAndWithoutTtl("cxId"))
                .expectNextCount(0);
    }
}