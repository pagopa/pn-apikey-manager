package it.pagopa.pn.apikey.manager.utils;

import it.pagopa.pn.apikey.manager.constant.ApiKeyConstant;
import it.pagopa.pn.apikey.manager.entity.ApiKeyModel;
import it.pagopa.pn.apikey.manager.repository.ApiKeyPageable;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.enhanced.dynamodb.model.Page;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Slf4j
@NoArgsConstructor(access = AccessLevel.NONE)
public class QueryUtils {

    public static ApiKeyPageable getNewPageable(Page<ApiKeyModel> page, ApiKeyPageable pageable) {
        return ApiKeyPageable.builder()
                .lastEvaluatedKey(page.lastEvaluatedKey().get(ApiKeyConstant.PK).s())
                .lastEvaluatedLastUpdate(page.lastEvaluatedKey().get(ApiKeyConstant.LAST_UPDATE).s())
                .limit(pageable.getLimit())
                .build();
    }

    public static List<ApiKeyModel> adjustPageResult(List<ApiKeyModel> result,
                                                     ApiKeyPageable pageable,
                                                     Map<String, AttributeValue> lastEvaluatedKey) {
        if (pageable.hasLimit() && result.size() > pageable.getLimit()) {
            log.debug("need to truncate last page - size from {} to {}", result.size(), pageable.getLimit());
            result = result.subList(0, pageable.getLimit());
            if (!result.isEmpty() && lastEvaluatedKey != null) {
                log.debug("need to adjust last evaluated key from {}", lastEvaluatedKey);
                ApiKeyModel lastElement = result.get(result.size() - 1);
                log.debug("last element is {}", lastElement);
                lastEvaluatedKey.put(ApiKeyConstant.PK, AttributeValue.builder().s(lastElement.getId()).build());
                if (lastElement.getLastUpdate() != null) {
                    lastEvaluatedKey.put(ApiKeyConstant.LAST_UPDATE, AttributeValue.builder().s(lastElement.getLastUpdate()
                            .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)).build());
                }
                log.debug("new last evaluated key is {}", lastEvaluatedKey);
            }
        }
        return result;
    }

}
