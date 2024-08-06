package it.pagopa.pn.apikey.manager.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import it.pagopa.pn.apikey.manager.model.PnLastEvaluatedKey;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.util.Map;

class PnLastEvaluatedKeyTest {

    @Test
    void deserializeSerializelLastEvaluatedKey() throws JsonProcessingException {
        //Given
        PnLastEvaluatedKey lastEvaluatedKeyToSerialize = new PnLastEvaluatedKey();
        lastEvaluatedKeyToSerialize.setExternalLastEvaluatedKey( "SenderId##creationMonth" );
        lastEvaluatedKeyToSerialize.setInternalLastEvaluatedKey(
                Map.of( "KEY", AttributeValue.builder()
                        .s( "VALUE" )
                .build() )  );

        //When
        String serializedLEK = lastEvaluatedKeyToSerialize.serializeInternalLastEvaluatedKey();
        PnLastEvaluatedKey deserializedLEK = PnLastEvaluatedKey.deserializeInternalLastEvaluatedKey( serializedLEK );

        //Then
        Assertions.assertEquals(lastEvaluatedKeyToSerialize.getExternalLastEvaluatedKey(), deserializedLEK.getExternalLastEvaluatedKey());
        Assertions.assertEquals(lastEvaluatedKeyToSerialize.getInternalLastEvaluatedKey(), deserializedLEK.getInternalLastEvaluatedKey());
    }
}
