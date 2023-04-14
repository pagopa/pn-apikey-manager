package it.pagopa.pn.apikey.manager.constant;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.NONE)
public final class PaAggregationConstant {
    public static final String PK = "x-pagopa-pn-cx-id";
    public static final String AGGREGATE_ID = "aggregateId";
    public static final String PA_NAME = "paName";
    public static final String PA_ID = "x-pagopa-pn-cx-id";
    public static final String PAGEABLE = "pageable";
    public static final String PAGEABLE_VALUE = "Y";
    public static final String GSI_AGGREGATE_ID = "paAggregations-aggregateId-index";
    public static final String GSI_PAGEABLE_PA_NAME = "pageable-paName-index";

}
