package it.pagopa.pn.apikey.manager.service;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;

import it.pagopa.pn.apikey.manager.client.ExternalRegistriesClient;
import it.pagopa.pn.apikey.manager.entity.PaAggregationModel;
import it.pagopa.pn.apikey.manager.generated.openapi.rest.v1.aggregate.dto.AddPaListRequestDto;
import it.pagopa.pn.apikey.manager.generated.openapi.rest.v1.aggregate.dto.AssociablePaResponseDto;
import it.pagopa.pn.apikey.manager.generated.openapi.rest.v1.aggregate.dto.MovePaResponseDto;
import it.pagopa.pn.apikey.manager.generated.openapi.rest.v1.aggregate.dto.PaDetailDto;
import it.pagopa.pn.apikey.manager.model.PnBatchGetItemResponse;
import it.pagopa.pn.apikey.manager.model.PnBatchPutItemResponse;
import it.pagopa.pn.apikey.manager.repository.PaAggregationRepository;
import it.pagopa.pn.apikey.manager.utils.DynamoBatchResponseUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import software.amazon.awssdk.enhanced.dynamodb.model.BatchGetResultPage;
import software.amazon.awssdk.enhanced.dynamodb.model.BatchWriteResult;
import software.amazon.awssdk.enhanced.dynamodb.model.Page;
import software.amazon.awssdk.services.dynamodb.model.BatchGetItemResponse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@ContextConfiguration(classes = {PaService.class})
@ExtendWith(SpringExtension.class)
class PaServiceTest {

    @Autowired
    private PaService paService;

    @MockBean
    private PaAggregationRepository paAggregationRepository;

    @MockBean
    private DynamoBatchResponseUtils dynamoBatchResponseUtils;

    @MockBean
    private ExternalRegistriesClient externalRegistriesClient;

    @Test
    void testGetAssociablePa() {
        AssociablePaResponseDto associablePaResponseDto = new AssociablePaResponseDto();
        List<PaDetailDto> list = new ArrayList<>();
        PaDetailDto paDetailDto = new PaDetailDto();
        paDetailDto.setName("name");
        paDetailDto.setId("id");
        list.add(paDetailDto);
        associablePaResponseDto.setItems(list);
        when(externalRegistriesClient.getAllPa(any())).thenReturn(Mono.just(list));
        List<PaAggregationModel> models = new ArrayList<>();
        PaAggregationModel paAggregationModel = new PaAggregationModel();
        paAggregationModel.setPaId("id");
        paAggregationModel.setAggregateId("id");
        paAggregationModel.setPaName("name");
        models.add(paAggregationModel);
        Page<PaAggregationModel> page = Page.create(models);
        when(paAggregationRepository.getAllPaAggregations()).thenReturn(Mono.just(page));
        StepVerifier.create(paService.getAssociablePa("name"))
                .expectNext(associablePaResponseDto).verifyComplete();
    }

    /**
     * Method under test: {@link PaService#movePa(String, AddPaListRequestDto)}
     */
    @Test
    void testMovePa() {
        List<PaAggregationModel> paAggregationModels = new ArrayList<>();
        PaAggregationModel paAggregationModel = new PaAggregationModel();
        paAggregationModel.setAggregateId("id");
        paAggregationModel.setPaName("name");
        paAggregationModel.setPaId("id");
        paAggregationModels.add(paAggregationModel);

        PnBatchGetItemResponse pnBatchGetItemResponse = new PnBatchGetItemResponse();
        pnBatchGetItemResponse.setUnprocessed(new ArrayList<>());
        pnBatchGetItemResponse.setFounded(paAggregationModels);
        when(dynamoBatchResponseUtils.convertPaAggregationsBatchGetItemResponse(any()))
                .thenReturn(pnBatchGetItemResponse);

        PnBatchPutItemResponse pnBatchPutItemResponse = new PnBatchPutItemResponse();
        pnBatchPutItemResponse.setUnprocessed(new ArrayList<>());
        when(dynamoBatchResponseUtils.convertPaAggregationsBatchPutItemResponse(any()))
                .thenReturn(pnBatchPutItemResponse);

        BatchGetResultPage batchGetResultPage = BatchGetResultPage.builder()
                .batchGetItemResponse(BatchGetItemResponse.builder()
                        .responses(new HashMap<>())
                        .unprocessedKeys(new HashMap<>())
                        .build()).build();
        when(paAggregationRepository.batchGetItem(any())).thenReturn(Flux.just(batchGetResultPage));

        BatchWriteResult batchWriteResult = BatchWriteResult.builder()
                .unprocessedRequests(new HashMap<>())
                .build();
        when(paAggregationRepository.savePaAggregation(anyList())).thenReturn(Flux.just(batchWriteResult));

        AddPaListRequestDto addPaListRequestDto = new AddPaListRequestDto();
        List<PaDetailDto> list = new ArrayList<>();
        PaDetailDto paDetailDto = new PaDetailDto();
        paDetailDto.setId("id");
        paDetailDto.setName("name");
        list.add(paDetailDto);
        addPaListRequestDto.setItems(list);

        MovePaResponseDto movePaResponseDto = new MovePaResponseDto();
        movePaResponseDto.setUnprocessedPA(new ArrayList<>());
        movePaResponseDto.setProcessed(1);
        StepVerifier.create(paService.movePa("foo", addPaListRequestDto))
                .expectNext(movePaResponseDto).verifyComplete();
    }


    @Test
    void testCreateNewPaAggregation(){
        List<PaAggregationModel> paAggregationModels = new ArrayList<>();
        PaAggregationModel paAggregationModel = new PaAggregationModel();
        paAggregationModel.setAggregateId("id");
        paAggregationModel.setPaName("name");
        paAggregationModel.setPaId("id");
        paAggregationModels.add(paAggregationModel);

        PnBatchGetItemResponse pnBatchGetItemResponse = new PnBatchGetItemResponse();
        pnBatchGetItemResponse.setUnprocessed(new ArrayList<>());
        pnBatchGetItemResponse.setFounded(paAggregationModels);
        when(dynamoBatchResponseUtils.convertPaAggregationsBatchGetItemResponse(any()))
                .thenReturn(pnBatchGetItemResponse);

        PnBatchPutItemResponse pnBatchPutItemResponse = new PnBatchPutItemResponse();
        pnBatchPutItemResponse.setUnprocessed(new ArrayList<>());
        when(dynamoBatchResponseUtils.convertPaAggregationsBatchPutItemResponse(any()))
                .thenReturn(pnBatchPutItemResponse);

        BatchGetResultPage batchGetResultPage = BatchGetResultPage.builder()
                .batchGetItemResponse(BatchGetItemResponse.builder()
                        .responses(new HashMap<>())
                        .unprocessedKeys(new HashMap<>())
                        .build()).build();
        when(paAggregationRepository.batchGetItem(any())).thenReturn(Flux.just(batchGetResultPage));

        BatchWriteResult batchWriteResult = BatchWriteResult.builder()
                .unprocessedRequests(new HashMap<>())
                .build();
        when(paAggregationRepository.savePaAggregation(anyList())).thenReturn(Flux.just(batchWriteResult));

        AddPaListRequestDto addPaListRequestDto = new AddPaListRequestDto();
        List<PaDetailDto> list = new ArrayList<>();
        PaDetailDto paDetailDto = new PaDetailDto();
        paDetailDto.setId("id");
        paDetailDto.setName("name");
        list.add(paDetailDto);
        addPaListRequestDto.setItems(list);

        MovePaResponseDto movePaResponseDto = new MovePaResponseDto();
        movePaResponseDto.setUnprocessedPA(new ArrayList<>());
        movePaResponseDto.setProcessed(1);

        StepVerifier.create(paService.createNewPaAggregation("foo", addPaListRequestDto))
                .expectNext(movePaResponseDto).verifyComplete();

    }
}

