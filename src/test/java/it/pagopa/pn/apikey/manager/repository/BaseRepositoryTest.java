package it.pagopa.pn.apikey.manager.repository;

import it.pagopa.pn.apikey.manager.model.PnLastEvaluatedKey;
import it.pagopa.pn.apikey.manager.model.ResultPaginationDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import software.amazon.awssdk.core.async.SdkPublisher;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbAsyncTable;
import software.amazon.awssdk.enhanced.dynamodb.model.Page;
import software.amazon.awssdk.enhanced.dynamodb.model.PagePublisher;
import software.amazon.awssdk.enhanced.dynamodb.model.ScanEnhancedRequest;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static org.mockito.Mockito.*;

class BaseRepositoryTest {

    @Mock
    private DynamoDbAsyncTable<DummyEntity> dynamoDbAsyncTable;
    private BaseRepository<DummyEntity> baseRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        baseRepository = new BaseRepository(dynamoDbAsyncTable);
    }

    @Test
    void scanByFilterPaginatedReturnsResultsWithinLimit() {
        ScanEnhancedRequest request = ScanEnhancedRequest.builder().build();
        ResultPaginationDto<DummyEntity> resultPaginationDto = new ResultPaginationDto<>();
        Map<String, AttributeValue> lastEvaluatedKey = new HashMap<>();
        Function<DummyEntity, PnLastEvaluatedKey> keyMaker = mock(Function.class);
        when(keyMaker.apply(any())).thenReturn(new PnLastEvaluatedKey());
        SdkPublisher<Page<DummyEntity>> sdkPublisher = mock(SdkPublisher.class);
        doAnswer(invocation -> {
            DummyEntity entity1 = new DummyEntity("id1", "name1");
            DummyEntity entity2 = new DummyEntity("id2", "name2");

            List<DummyEntity> entities = Arrays.asList(entity1, entity2);

            Page<DummyEntity> page = Page.create(entities, null);

            Subscriber<? super Page<DummyEntity>> subscriber = invocation.getArgument(0);
            subscriber.onSubscribe(new Subscription() {
                @Override
                public void request(long n) {
                    if (n != 0) {
                        subscriber.onNext(page);
                        subscriber.onComplete();
                    }
                }

                @Override
                public void cancel() {
                }
            });
            return null;
        }).when(sdkPublisher).subscribe((Subscriber<? super Page<DummyEntity>>) any());

        PagePublisher<DummyEntity> pagePublisher = PagePublisher.create(sdkPublisher);
        when(dynamoDbAsyncTable.scan((ScanEnhancedRequest) any())).thenReturn(pagePublisher);

        Mono<Page<DummyEntity>> result = baseRepository.scanByFilterPaginated(request, resultPaginationDto, 2, lastEvaluatedKey, keyMaker);

        StepVerifier.create(result)
                .expectNextMatches(p -> p.items().size() == 2)
                .verifyComplete();
    }

    class DummyEntity {
        private String id;
        private String name;

        public DummyEntity(String id, String name) {
            this.id = id;
            this.name = name;
        }

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public void setId(String id) {
            this.id = id;
        }
    }
}