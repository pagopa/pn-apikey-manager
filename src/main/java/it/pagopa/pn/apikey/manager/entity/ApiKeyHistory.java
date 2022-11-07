package it.pagopa.pn.apikey.manager.entity;

import lombok.Data;
import lombok.Getter;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;

import java.time.LocalDateTime;

@Data
@DynamoDbBean
public class ApiKeyHistory {

    @Getter(onMethod=@__({@DynamoDbAttribute("status")}))
    private String status;

    @Getter(onMethod=@__({@DynamoDbAttribute("date")}))
    private LocalDateTime date;

    @Getter(onMethod=@__({@DynamoDbAttribute("changeByDenomination")}))
    private String changeByDenomination;

}
