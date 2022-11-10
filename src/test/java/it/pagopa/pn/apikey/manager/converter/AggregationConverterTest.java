package it.pagopa.pn.apikey.manager.converter;

import it.pagopa.pn.apikey.manager.entity.ApiKeyAggregateModel;
import it.pagopa.pn.apikey.manager.generated.openapi.rest.v1.aggregate.dto.AggregatesListResponseDto;
import it.pagopa.pn.apikey.manager.generated.openapi.rest.v1.aggregate.dto.UsagePlanDetailDto;

import java.time.LocalDateTime;

import java.util.HashMap;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import software.amazon.awssdk.enhanced.dynamodb.model.Page;

@ContextConfiguration(classes = {AggregationConverter.class})
@ExtendWith(SpringExtension.class)
class AggregationConverterTest {

    @Autowired
    private AggregationConverter aggregationConverter;

    @Test
    void testConvertResponseDto2() {
        ApiKeyAggregateModel apiKeyAggregateModel = new ApiKeyAggregateModel();
        apiKeyAggregateModel.setAggregateId("id");
        apiKeyAggregateModel.setApiKey("Api Key");
        apiKeyAggregateModel.setApiKeyId("42");
        apiKeyAggregateModel.setCreatedAt(LocalDateTime.of(1, 1, 1, 1, 1));
        apiKeyAggregateModel.setDescription("The characteristics of someone or something");
        apiKeyAggregateModel.setLastUpdate(LocalDateTime.of(1, 1, 1, 1, 1));
        apiKeyAggregateModel.setName("Name");
        apiKeyAggregateModel.setUsagePlanId("id");
        Page<ApiKeyAggregateModel> page = Page.create(List.of(apiKeyAggregateModel));
        HashMap<String,UsagePlanDetailDto> map = new HashMap<>();
        UsagePlanDetailDto usagePlanDetailDto = new UsagePlanDetailDto();
        usagePlanDetailDto.setId("id");
        usagePlanDetailDto.setName("name");
        map.put("id",usagePlanDetailDto);
        AggregatesListResponseDto aggregatesListResponseDto = aggregationConverter.convertResponseDto(page,map);
        Assertions.assertEquals(1,aggregatesListResponseDto.getItems().size());


    }
}

