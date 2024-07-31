package it.pagopa.pn.apikey.manager.model;

import lombok.Data;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
public class ResultPaginationDto<T> {
    private List<T> resultsPage = new ArrayList<>();
    private Map<String, AttributeValue> lastEvaluatedKey;

    public void addResults(List<T> results) {
        this.resultsPage.addAll(results);
    }

    public void setLastEvaluatedKey(Map<String, AttributeValue> lastEvaluatedKey) {
        this.lastEvaluatedKey = lastEvaluatedKey;
    }
}
