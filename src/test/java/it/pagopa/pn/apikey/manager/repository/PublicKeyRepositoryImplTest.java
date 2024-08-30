package it.pagopa.pn.apikey.manager.repository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.reactivestreams.Subscriber;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.test.StepVerifier;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbAsyncIndex;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbAsyncTable;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedAsyncClient;
import software.amazon.awssdk.enhanced.dynamodb.model.Page;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryEnhancedRequest;

import java.util.ArrayList;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
class PublicKeyRepositoryImplTest {

    @MockBean
    private DynamoDbAsyncIndex<Object> index;

    @MockBean
    private DynamoDbEnhancedAsyncClient dynamoDbEnhancedAsyncClient;

    @MockBean
    private DynamoDbAsyncTable<Object> dynamoDbAsyncTable;

    @Test
    void getAllPaginated_withValidCxIdAndPageable_returnsMonoOfPage() {
        when(dynamoDbEnhancedAsyncClient.table(any(), any())).thenReturn(dynamoDbAsyncTable);
        PublicKeyRepositoryImpl publicKeyRepository = new PublicKeyRepositoryImpl(dynamoDbEnhancedAsyncClient, "");

        when(dynamoDbAsyncTable.index(any())).thenReturn(index);
        when(index.query((QueryEnhancedRequest) any())).thenReturn(Subscriber::onComplete);

        PublicKeyPageable pageable = PublicKeyPageable.builder()
                .lastEvaluatedKey("lastEvaluatedKey")
                .createdAt("createdAt")
                .limit(10)
                .build();

        StepVerifier.create(publicKeyRepository.getAllPaginated("cxId", pageable, any()))
                .expectNext(Page.create(new ArrayList<>()));
    }
}