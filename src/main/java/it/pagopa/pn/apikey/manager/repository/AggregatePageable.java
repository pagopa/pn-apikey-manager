package it.pagopa.pn.apikey.manager.repository;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import org.springframework.util.StringUtils;

@Getter
@ToString
@Builder
public class AggregatePageable {

    private Integer limit;
    private String lastEvaluatedId;

    public boolean isPageable() {
        return StringUtils.hasText(lastEvaluatedId);
    }

}
