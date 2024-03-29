package it.pagopa.pn.apikey.manager.entity;

import it.pagopa.pn.apikey.manager.constant.PaAggregationConstant;
import lombok.Data;
import lombok.Getter;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.*;

@Data
@DynamoDbBean
public class PaAggregationModel {

    @Getter(onMethod=@__({
            @DynamoDbPartitionKey,
            @DynamoDbAttribute(PaAggregationConstant.PA_ID),
            @DynamoDbSecondarySortKey(indexNames = {PaAggregationConstant.GSI_AGGREGATE_ID})
    }))
    private String paId;

    @Getter(onMethod=@__({
            @DynamoDbAttribute(PaAggregationConstant.AGGREGATE_ID),
            @DynamoDbSecondaryPartitionKey(indexNames = PaAggregationConstant.GSI_AGGREGATE_ID)
    }))
    private String aggregateId;

    @Getter(onMethod = @__({
            @DynamoDbAttribute(PaAggregationConstant.PA_NAME),
            @DynamoDbSecondarySortKey(indexNames = {PaAggregationConstant.GSI_PAGEABLE_PA_NAME})
    }))
    private String paName;

    @Getter(onMethod = @__({
            @DynamoDbAttribute(PaAggregationConstant.PAGEABLE),
            @DynamoDbSecondaryPartitionKey(indexNames = PaAggregationConstant.GSI_PAGEABLE_PA_NAME)
    }))
    private String pageable = PaAggregationConstant.PAGEABLE_VALUE;
}
