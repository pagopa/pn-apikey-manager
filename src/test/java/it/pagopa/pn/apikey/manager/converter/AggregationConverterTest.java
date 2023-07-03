package it.pagopa.pn.apikey.manager.converter;

import it.pagopa.pn.apikey.manager.entity.ApiKeyAggregateModel;
import it.pagopa.pn.apikey.manager.entity.PaAggregationModel;
import it.pagopa.pn.apikey.manager.generated.openapi.server.v1.aggregate.dto.*;

import java.time.LocalDateTime;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import software.amazon.awssdk.enhanced.dynamodb.model.Page;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

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
        List<UsagePlanDetailDto> map = new ArrayList<>();
        UsagePlanDetailDto usagePlanDetailDto = new UsagePlanDetailDto();
        usagePlanDetailDto.setId("id");
        usagePlanDetailDto.setName("name");
        map.add(usagePlanDetailDto);
        AggregatesListResponseDto aggregatesListResponseDto = aggregationConverter.convertToResponseDto(page, map);
        assertEquals(1, aggregatesListResponseDto.getItems().size());
    }

    @Test
    void converResponseDto2() {
        ApiKeyAggregateModel apiKeyAggregateModel = new ApiKeyAggregateModel();
        apiKeyAggregateModel.setAggregateId("id");
        apiKeyAggregateModel.setUsagePlanId("id");
        apiKeyAggregateModel.setCreatedAt(LocalDateTime.now());
        apiKeyAggregateModel.setLastUpdate(LocalDateTime.now());

        UsagePlanDetailDto usagePlanDetailDto = new UsagePlanDetailDto();
        usagePlanDetailDto.setId("id");
        AggregateResponseDto aggregatesResponseDto = aggregationConverter.convertToResponseDto(apiKeyAggregateModel, usagePlanDetailDto);
        assertEquals("id", aggregatesResponseDto.getId());
    }

    @Test
    void converResponseDto3() {
        PaAggregationModel paAggregationModel = new PaAggregationModel();
        paAggregationModel.setAggregateId("id");
        paAggregationModel.setPaName("name");
        paAggregationModel.setPaId("paId");

        Page<PaAggregationModel> page = Page.create(List.of(paAggregationModel));

        PaAggregateResponseDto paAggregateResponseDto = aggregationConverter.convertToResponseDto(page);
        assertEquals(1, paAggregateResponseDto.getItems().size());
    }

    @Test
    void testConvertToModel() {
        AggregateRequestDto dto = new AggregateRequestDto();
        dto.setName("name");
        dto.setDescription("desc");
        dto.setUsagePlanId("usagePlan");
        ApiKeyAggregateModel model = aggregationConverter.convertToModel(dto);
        assertNotNull(model.getAggregateId());
        assertEquals("name", model.getName());
        assertEquals("desc", model.getDescription());
        assertEquals("usagePlan", model.getUsagePlanId());
        assertNotNull(model.getLastUpdate());
        assertNotNull(model.getCreatedAt());
    }
}
