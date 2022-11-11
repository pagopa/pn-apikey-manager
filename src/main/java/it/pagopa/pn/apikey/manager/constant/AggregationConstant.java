package it.pagopa.pn.apikey.manager.constant;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.NONE)
public final class AggregationConstant {
    public static final String PK = "aggregateId";
    public static final String NAME = "aggregateName"; // TODO cambiare in name
    public static final String GSI_NAME = "aggregations-aggregateName-index";
    public static final String PAGEABLE = "pageable";
    public static final String PAGEABLE_VALUE = "Y";
}
