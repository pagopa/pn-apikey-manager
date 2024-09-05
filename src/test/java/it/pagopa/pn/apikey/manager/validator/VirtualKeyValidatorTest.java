package it.pagopa.pn.apikey.manager.validator;

import it.pagopa.pn.apikey.manager.entity.ApiKeyModel;
import it.pagopa.pn.apikey.manager.exception.ApiKeyManagerException;
import it.pagopa.pn.apikey.manager.generated.openapi.server.v1.dto.ApiKeyStatusDto;
import it.pagopa.pn.apikey.manager.generated.openapi.server.v1.dto.CxTypeAuthFleetDto;
import it.pagopa.pn.apikey.manager.repository.ApiKeyRepository;
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
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedAsyncClient;
import software.amazon.awssdk.enhanced.dynamodb.model.Page;

import java.util.List;

import static org.mockito.Mockito.when;

@SpringBootTest
@ExtendWith(SpringExtension.class)
class VirtualKeyValidatorTest {

    @Autowired
    private VirtualKeyValidator validator;
    @MockBean
    private DynamoDbEnhancedAsyncClient dynamoDbEnhancedAsyncClient;

    @MockBean
    private ApiKeyRepository apiKeyRepository;


    @BeforeEach
    void setUp() {
        validator = new VirtualKeyValidator(apiKeyRepository);
    }

    @Test
    void validateCxType_shouldReturnError_whenCxTypeIsNotPG() {
        CxTypeAuthFleetDto cxType = CxTypeAuthFleetDto.PA; // Use a non-PG value

        Mono<Void> result = validator.validateCxType(cxType);

        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof ApiKeyManagerException &&
                        throwable.getMessage().equals("Error, cxType must be PG") &&
                        ((ApiKeyManagerException) throwable).getStatus() == HttpStatus.FORBIDDEN)
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
    void checkCxIdAndUid_shouldReturnError_whenCxIdAndUidDoesNotMatch() {
        ApiKeyModel apiKeyModel = new ApiKeyModel();
        apiKeyModel.setCxId("differentCxId");
        apiKeyModel.setUid("differentUid");

        Mono<ApiKeyModel> result = validator.checkCxIdAndUid("testCxId", "testUid", apiKeyModel);

        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof ApiKeyManagerException &&
                        throwable.getMessage().equals("CxId or uId does not match") &&
                        ((ApiKeyManagerException) throwable).getStatus() == HttpStatus.FORBIDDEN)
                .verify();
    }

    @Test
    void checkCxId_shouldReturnApiKey_whenCxIdAndUidMatches() {
        ApiKeyModel apiKeyModel = new ApiKeyModel();
        apiKeyModel.setCxId("testCxId");
        apiKeyModel.setUid("testUid");

        Mono<ApiKeyModel> result = validator.checkCxIdAndUid("testCxId", "testUid", apiKeyModel);

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
                        ((ApiKeyManagerException) throwable).getStatus() == HttpStatus.CONFLICT)
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

    @Test
    void checkExistingRotatedKeys_shouldReturnError_whenRotatedKeyExists() {
        String xPagopaPnUid = "testUid";
        String xPagopaPnCxId = "testCxId";
        ApiKeyModel rotatedKey = new ApiKeyModel();
        rotatedKey.setCxId(xPagopaPnCxId);
        rotatedKey.setUid(xPagopaPnUid);
        rotatedKey.setStatus(ApiKeyStatusDto.ROTATED.toString());

        Page<ApiKeyModel> rotatedKeysPage = Page.create(List.of(rotatedKey));

        when(apiKeyRepository.findByUidAndCxIdAndStatusAndScope(xPagopaPnUid, xPagopaPnCxId, ApiKeyStatusDto.ROTATED.toString(), ApiKeyModel.Scope.CLIENTID.toString()))
                .thenReturn(Mono.just(rotatedKeysPage));

        Mono<Void> result = validator.checkExistingRotatedKeys(xPagopaPnUid, xPagopaPnCxId);

        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof ApiKeyManagerException &&
                        throwable.getMessage().equals("User already has a rotated key with the same CxId") &&
                        ((ApiKeyManagerException) throwable).getStatus() == HttpStatus.CONFLICT)
                .verify();
    }

    @Test
    void checkExistingRotatedKeys_shouldComplete_whenNoRotatedKeyExists() {
        String xPagopaPnUid = "testUid";
        String xPagopaPnCxId = "testCxId";

        Page<ApiKeyModel> emptyRotatedKeysPage = Page.create(List.of());

        when(apiKeyRepository.findByUidAndCxIdAndStatusAndScope(xPagopaPnUid, xPagopaPnCxId, ApiKeyStatusDto.ROTATED.toString(), ApiKeyModel.Scope.CLIENTID.toString()))
                .thenReturn(Mono.just(emptyRotatedKeysPage));

        Mono<Void> result = validator.checkExistingRotatedKeys(xPagopaPnUid, xPagopaPnCxId);

        StepVerifier.create(result)
                .verifyComplete();
    }
}
