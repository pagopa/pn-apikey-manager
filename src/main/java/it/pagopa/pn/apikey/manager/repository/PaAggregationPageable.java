package it.pagopa.pn.apikey.manager.repository;

import lombok.*;
import org.springframework.util.StringUtils;

@Getter
@ToString
@Builder
public class PaAggregationPageable implements Pageable {

    private Integer limit;
    private String lastEvaluatedKey;

    public static PaAggregationPageable createEmpty() {
        return new PaAggregationPageable(null, null);
    }

    public static PaAggregationPageable createWithLimit(Integer limit) {
        return new PaAggregationPageable(limit, null);
    }

    @Override
    public boolean isPage() {
        return StringUtils.hasText(lastEvaluatedKey);
    }

    @Override
    public boolean hasLimit() {
        return limit != null && limit > 0;
    }
}
