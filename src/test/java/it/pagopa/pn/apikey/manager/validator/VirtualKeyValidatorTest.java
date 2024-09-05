package it.pagopa.pn.apikey.manager.validator;

import it.pagopa.pn.apikey.manager.entity.ApiKeyModel;
import it.pagopa.pn.apikey.manager.exception.ApiKeyManagerException;
import it.pagopa.pn.apikey.manager.generated.openapi.server.v1.dto.ApiKeyStatusDto;
import it.pagopa.pn.apikey.manager.generated.openapi.server.v1.dto.CxTypeAuthFleetDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbAsyncIndex;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbAsyncTable;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedAsyncClient;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest
@ExtendWith(SpringExtension.class)
class VirtualKeyValidatorTest {

    @Autowired
    private VirtualKeyValidator validator;
    @MockBean
    private DynamoDbEnhancedAsyncClient dynamoDbEnhancedAsyncClient;

    @MockBean
    private DynamoDbAsyncTable<Object> dynamoDbAsyncTable;

    private DynamoDbAsyncIndex<Object> index;


    @BeforeEach
    void setUp() {
        validator = new VirtualKeyValidator();
        index = mock(DynamoDbAsyncIndex.class);
        when(dynamoDbAsyncTable.index(any())).thenReturn(index);
    }

    @Test
    void validateCxType_shouldReturnError_whenCxTypeIsNotPG() {
        CxTypeAuthFleetDto cxType = CxTypeAuthFleetDto.PA; // Use a non-PG value

        Mono<Void> result = validator.validateCxType(cxType);

        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof ApiKeyManagerException &&
                        throwable.getMessage().equals("Error, cxType must be PG") &&
                        ((ApiKeyManagerException) throwable).getStatus() == HttpStatus.BAD_REQUEST)
                .verify();
    }

    @Test
    void validateCxType_shouldComplete_whenCxTypeIsPG() {
        CxTypeAuthFleetDto cxType = CxTypeAuthFleetDto.PG;
        Mono<Void> result = validator.validateCxType(cxType);

        StepVerifier.create(result)
                .verifyComplete();
    }

    @Test
    void checkCxId_shouldReturnError_whenCxIdDoesNotMatch() {
        ApiKeyModel apiKeyModel = new ApiKeyModel();
        apiKeyModel.setCxId("differentCxId");

        Mono<ApiKeyModel> result = validator.checkCxId("testCxId", apiKeyModel);

        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof ApiKeyManagerException &&
                        throwable.getMessage().equals("CxId does not match") &&
                        ((ApiKeyManagerException) throwable).getStatus() == HttpStatus.BAD_REQUEST)
                .verify();
    }

    @Test
    void checkCxId_shouldReturnApiKey_whenCxIdMatches() {
        ApiKeyModel apiKeyModel = new ApiKeyModel();
        apiKeyModel.setCxId("testCxId");

        Mono<ApiKeyModel> result = validator.checkCxId("testCxId", apiKeyModel);

        StepVerifier.create(result)
                .expectNext(apiKeyModel)
                .verifyComplete();
    }

    @Test
    void checkStatus_shouldReturnError_whenStatusIsNotEnabled() {
        ApiKeyModel apiKeyModel = new ApiKeyModel();
        apiKeyModel.setStatus("DISABLED");

        Mono<ApiKeyModel> result = validator.checkStatus(apiKeyModel);

        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof ApiKeyManagerException &&
                        throwable.getMessage().equals("virtualKey is not in enabled state") &&
                        ((ApiKeyManagerException) throwable).getStatus() == HttpStatus.BAD_REQUEST)
                .verify();
    }

    @Test
    void checkStatus_shouldReturnApiKey_whenStatusIsEnabled() {
        ApiKeyModel apiKeyModel = new ApiKeyModel();
        apiKeyModel.setStatus(ApiKeyStatusDto.ENABLED.toString());

        Mono<ApiKeyModel> result = validator.checkStatus(apiKeyModel);

        StepVerifier.create(result)
                .expectNext(apiKeyModel)
                .verifyComplete();
    }
}
