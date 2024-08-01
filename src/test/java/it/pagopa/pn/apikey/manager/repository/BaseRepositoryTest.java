package it.pagopa.pn.apikey.manager.repository;

import it.pagopa.pn.apikey.manager.model.PnLastEvaluatedKey;
import it.pagopa.pn.apikey.manager.model.ResultPaginationDto;
import lombok.Getter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbAsyncTable;
import software.amazon.awssdk.enhanced.dynamodb.model.Page;
import software.amazon.awssdk.enhanced.dynamodb.model.ScanEnhancedRequest;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.function.Function;

import static org.mockito.Mockito.*;

class BaseRepositoryTest {

    @Mock
    private DynamoDbAsyncTable<DummyEntity> dynamoDbAsyncTable;
    private BaseRepository<DummyEntity> baseRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        baseRepository = new BaseRepository<>(dynamoDbAsyncTable);
    }

    @Test
    void scanByFilterPaginatedReturnsResultsWithinLimit() {
        ScanEnhancedRequest request = ScanEnhancedRequest.builder().build();
        Function<DummyEntity, PnLastEvaluatedKey> keyMaker = mock(Function.class);
        when(keyMaker.apply(any())).thenReturn(new PnLastEvaluatedKey());

        DummyEntity entity1 = new DummyEntity("id1", "name1");
        DummyEntity entity2 = new DummyEntity("id2", "name2");

        List<DummyEntity> entities = Arrays.asList(entity1, entity2);

        TestUtilsRepository.mockScanEnhancedRequestToRetrievePage(dynamoDbAsyncTable, entities);

        Mono<Page<DummyEntity>> result = baseRepository.scanByFilterPaginated(request, new ResultPaginationDto<>(), 2, new HashMap<>(), keyMaker);

        StepVerifier.create(result)
                .expectNextMatches(p -> p.items().size() == 2)
                .verifyComplete();
    }

    @Getter
    static class DummyEntity {
        private String id;
        private String name;

        public DummyEntity(String id, String name) {
            this.id = id;
            this.name = name;
        }
    }
}