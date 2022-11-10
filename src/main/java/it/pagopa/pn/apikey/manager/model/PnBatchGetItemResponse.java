package it.pagopa.pn.apikey.manager.model;

import it.pagopa.pn.apikey.manager.entity.PaAggregationModel;
import lombok.Data;
import software.amazon.awssdk.enhanced.dynamodb.Key;

import java.util.ArrayList;
import java.util.List;

@Data
public class PnBatchGetItemResponse {

    private List<PaAggregationModel> founded = new ArrayList<>();
    private List<Key> unprocessed = new ArrayList<>();

}
