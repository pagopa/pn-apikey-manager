package it.pagopa.pn.apikey.manager.repository;

import it.pagopa.pn.apikey.manager.model.PnLastEvaluatedKey;
import it.pagopa.pn.apikey.manager.model.ResultPaginationDto;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbAsyncTable;
import software.amazon.awssdk.enhanced.dynamodb.model.Page;
import software.amazon.awssdk.enhanced.dynamodb.model.ScanEnhancedRequest;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class BaseRepository<T> {
    protected final DynamoDbAsyncTable<T> table;

    public BaseRepository(DynamoDbAsyncTable<T> table) {
        this.table = table;
    }

    /**
     * This function scans the table until the limit is reached or there are no more elements to scan.
     *
     * @param scanEnhancedRequest scan request
     * @param resultPaginationDto should be initialized with an empty list
     * @param limit               maximum number of elements to return
     * @param lastEvaluatedKey    last evaluated key from the previous scan
     * @param keyMaker            function to extract the last evaluated key from an element
     * @return
     */
    protected Mono<Page<T>> scanByFilterPaginated(
            ScanEnhancedRequest scanEnhancedRequest,
            ResultPaginationDto<T> resultPaginationDto,
            int limit,
            Map<String, AttributeValue> lastEvaluatedKey,
            Function<T, PnLastEvaluatedKey> keyMaker
    ) {
        if (lastEvaluatedKey != null) {
            scanEnhancedRequest = scanEnhancedRequest.toBuilder().exclusiveStartKey(lastEvaluatedKey).build();
        }

        ScanEnhancedRequest finalScanEnhancedRequest = scanEnhancedRequest;
        return Mono.from(table.scan(scanEnhancedRequest))
                .flatMap(tPage -> {
                    Map<String, AttributeValue> lastKey = tPage.lastEvaluatedKey();
                    resultPaginationDto.addResults(tPage.items());

                    // Breaking recursion
                    if (resultPaginationDto.getResultsPage().size() >= limit || lastKey == null) {
                        ResultPaginationDto<T> globalResult = prepareGlobalResult(resultPaginationDto.getResultsPage(), limit, keyMaker);
                        return Mono.just(Page.create(globalResult.getResultsPage(), globalResult.getLastEvaluatedKey()));
                    }
                    return scanByFilterPaginated(finalScanEnhancedRequest, resultPaginationDto, limit, lastKey, keyMaker);
                });

    }

    /**
     * This function slices the result list to the limit and extracts the last evaluated key from the last element.
     *
     * @param queryResult list of elements retrieved by the query
     * @param limit       maximum number of elements to return
     * @param keyMaker    function to extract the last evaluated key from an element
     * @return
     */
    protected ResultPaginationDto<T> prepareGlobalResult(List<T> queryResult, int limit, Function<T, PnLastEvaluatedKey> keyMaker) {
        ResultPaginationDto<T> resultPaginationDto = new ResultPaginationDto<>();
        resultPaginationDto.setResultsPage(queryResult);

        if (queryResult != null) {
            resultPaginationDto.setResultsPage(queryResult.stream()
                    .limit(limit > 0 ? limit : queryResult.size())
                    .toList());
        }

        if (limit > 0 && queryResult.size() >= limit) {
            resultPaginationDto.setLastEvaluatedKey(keyMaker.apply(queryResult.get(limit - 1)).getInternalLastEvaluatedKey());
        }
        return resultPaginationDto;
    }
}
