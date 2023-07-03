package it.pagopa.pn.apikey.manager.entity;

import lombok.Data;
import lombok.Getter;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;

@Data
@DynamoDbBean
public class UsagePlanModel {

    @Getter(onMethod = @__({@DynamoDbAttribute("id")}))
    private String id;

    @Getter(onMethod = @__({@DynamoDbAttribute("awsUsagePlanId")}))
    private String awsUsagePlanId;

    @Getter(onMethod = @__(@DynamoDbAttribute("name")))
    private String name;

    @Getter(onMethod = @__(@DynamoDbAttribute("description")))
    private String description;

    @Getter(onMethod = @__({@DynamoDbAttribute("rate")}))
    private Integer rate;

    @Getter(onMethod = @__({@DynamoDbAttribute("burst")}))
    private Integer burst;

    @Getter(onMethod = @__({@DynamoDbAttribute("quota")}))
    private Integer quota;

}
