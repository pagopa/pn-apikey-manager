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
    private String lastEvaluatedName;

    @Override
    public boolean isPage() {
        return StringUtils.hasText(lastEvaluatedId);
    }

    public boolean isPageByName() {
        return isPage() && StringUtils.hasText(lastEvaluatedName);
    }

    @Override
    public boolean hasLimit() {
        return limit != null && limit > 0;
    }
}
