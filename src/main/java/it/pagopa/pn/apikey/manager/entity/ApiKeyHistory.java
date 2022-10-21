package it.pagopa.pn.apikey.manager.entity;

import lombok.*;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;

@Data
@ToString
@DynamoDbBean
public class ApiKeyHistory {

    @Setter
    @Getter(onMethod=@__({@DynamoDbAttribute("status")}))
    private String status;

    @Setter @Getter(onMethod=@__({@DynamoDbAttribute("date")}))
    private String date;

    @Setter @Getter(onMethod=@__({@DynamoDbAttribute("changeByDenomination")}))
    private String changeByDenomination;

}
