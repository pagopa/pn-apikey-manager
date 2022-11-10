package it.pagopa.pn.apikey.manager.model;

import it.pagopa.pn.apikey.manager.entity.PaAggregationModel;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class PnBatchPutItemResponse {
    List<PaAggregationModel> unprocessed = new ArrayList<>();
}
