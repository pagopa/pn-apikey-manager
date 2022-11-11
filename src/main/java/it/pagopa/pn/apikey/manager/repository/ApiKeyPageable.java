package it.pagopa.pn.apikey.manager.repository;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import org.springframework.util.StringUtils;

@Getter
@ToString
@Builder
public class ApiKeyPageable implements Pageable {

    private Integer limit;
    private String lastEvaluatedKey;
    private String lastEvaluatedLastUpdate;

    @Override
    public boolean isPage() {
        return StringUtils.hasText(lastEvaluatedKey) && StringUtils.hasText(lastEvaluatedLastUpdate);
    }

    @Override
    public boolean hasLimit() {
        return limit != null && limit > 0;
    }
}
