package it.pagopa.pn.apikey.manager.repository;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import org.springframework.util.StringUtils;

@Getter
@ToString
@Builder
public class AggregatePageable implements Pageable {

    private Integer limit;
    private String lastEvaluatedId;

    @Override
    public boolean isPage() {
        return StringUtils.hasText(lastEvaluatedId);
    }

    @Override
    public boolean hasLimit() {
        return limit != null && limit > 0;
    }
}
