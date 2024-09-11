package it.pagopa.pn.apikey.manager.repository;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import org.springframework.util.StringUtils;

@Getter
@ToString
@Builder
public class PublicKeyPageable implements Pageable {

    private Integer limit;
    private String lastEvaluatedKey;
    private String lastEvaluatedCreatedAt;

    @Override
    public boolean isPage() {
        return StringUtils.hasText(lastEvaluatedKey) && StringUtils.hasText(lastEvaluatedCreatedAt);
    }

    @Override
    public boolean hasLimit() {
        return limit != null && limit > 0;
    }
}
