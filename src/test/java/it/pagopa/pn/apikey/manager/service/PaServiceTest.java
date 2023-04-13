package it.pagopa.pn.apikey.manager.service;

import it.pagopa.pn.apikey.manager.client.ExternalRegistriesClient;
import it.pagopa.pn.apikey.manager.entity.ApiKeyAggregateModel;
import it.pagopa.pn.apikey.manager.entity.PaAggregationModel;
import it.pagopa.pn.apikey.manager.generated.openapi.rest.v1.aggregate.dto.*;
import it.pagopa.pn.apikey.manager.model.PnBatchGetItemResponse;
import it.pagopa.pn.apikey.manager.model.PnBatchPutItemResponse;
import it.pagopa.pn.apikey.manager.repository.AggregateRepository;
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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;

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

    @MockBean
    private AggregateRepository aggregateRepository;

    @Test
    void getPaList2(){
        PaAggregationModel paAggregationModel = new PaAggregationModel();
        List<PaAggregationModel> paAggregationModels = new ArrayList<>();
        paAggregationModels.add(paAggregationModel);
        GetPaResponseDto getPaResponseDto = new GetPaResponseDto();
        List<PaDetailDto> list = new ArrayList<>();
        list.add(new PaDetailDto());
        getPaResponseDto.setItems(list);
        getPaResponseDto.setTotal(1);
        when(paAggregationRepository.getAllPageableWithFilter(any(),any())).thenReturn(Mono.just(Page.create(paAggregationModels)));
        StepVerifier.create(paService.getPa("paName",10,"lastKey"))
                .expectNext(getPaResponseDto)
                .verifyComplete();
    }

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
        when(paAggregationRepository.getAllPaAggregations()).thenReturn(Mono.just(Page.create(Collections.emptyList())));
        StepVerifier.create(paService.getAssociablePa("name"))
                .expectNext(associablePaResponseDto)
                .verifyComplete();
    }

    /**
     * Method under test: {@link PaService#movePa(String, MovePaListRequestDto)}
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

        when(aggregateRepository.getApiKeyAggregation(any()))
                .thenReturn(Mono.just(new ApiKeyAggregateModel()));

        MovePaListRequestDto movePaListRequestDto = new MovePaListRequestDto();
        List<PaMoveDetailDto> list = new ArrayList<>();
        PaMoveDetailDto paMoveDetailDto = new PaMoveDetailDto();
        paMoveDetailDto.setId("id");
        list.add(paMoveDetailDto);
        movePaListRequestDto.setItems(list);
        MovePaResponseDto movePaResponseDto = new MovePaResponseDto();
        movePaResponseDto.setUnprocessedPA(new ArrayList<>());
        movePaResponseDto.setProcessed(1);
        StepVerifier.create(paService.movePa("foo", movePaListRequestDto))
                .expectNext(movePaResponseDto)
                .verifyComplete();
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

        when(aggregateRepository.getApiKeyAggregation(any()))
                .thenReturn(Mono.just(new ApiKeyAggregateModel()));

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

