package it.pagopa.pn.apikey.manager.log;

import it.pagopa.pn.apikey.manager.utils.MaskDataUtils;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.core.interceptor.Context;
import software.amazon.awssdk.core.interceptor.ExecutionAttribute;
import software.amazon.awssdk.core.interceptor.ExecutionAttributes;
import software.amazon.awssdk.core.interceptor.ExecutionInterceptor;
import software.amazon.awssdk.services.dynamodb.model.*;

@Slf4j
public class AwsClientLoggerInterceptor implements ExecutionInterceptor {

    private static final String SERVICE_NAME = "ServiceName";
    private static final String OPERATION_NAME = "OperationName";
    private static final String START_TIME = "startTime";

    @Override
    public void beforeExecution(Context.BeforeExecution context, ExecutionAttributes executionAttributes) {
        log.info("START - {}.{} {}", executionAttributes.getAttributes().get(new ExecutionAttribute<>(SERVICE_NAME)),
                executionAttributes.getAttributes().get(new ExecutionAttribute<>(OPERATION_NAME)),
                MaskDataUtils.maskInformation(context.request().toString()));
        executionAttributes.putAttribute(new ExecutionAttribute<>(START_TIME), System.currentTimeMillis());
    }

    @Override
    public void afterExecution(Context.AfterExecution context, ExecutionAttributes executionAttributes) {
        Long startTime = executionAttributes.getAttribute(new ExecutionAttribute<>(START_TIME));
        Long elapsed = startTime != null ? System.currentTimeMillis() - startTime : null;

        Object serviceName = executionAttributes.getAttributes().get(new ExecutionAttribute<>(SERVICE_NAME));
        Object operationName = executionAttributes.getAttributes().get(new ExecutionAttribute<>(OPERATION_NAME));

        String maskedRequest = MaskDataUtils.maskInformation(context.request().toString());
        if (context.response() instanceof ScanResponse) {
            ScanResponse scanResponse = (ScanResponse) context.response();
            log.info("END - {}.{} request: {} count: {} timelapse: {} ms", serviceName, operationName, maskedRequest,
                    scanResponse.count(), elapsed);
        } else if (context.response() instanceof QueryResponse) {
            QueryResponse queryResponse = (QueryResponse) context.response();
            log.info("END - {}.{} request: {} count: {} timelapse: {} ms", serviceName, operationName, maskedRequest,
                    queryResponse.count(), elapsed);
        } else if (context.response() instanceof GetItemResponse) {
            GetItemResponse getItemResponse = (GetItemResponse) context.response();
            String maskedResponse = MaskDataUtils.maskInformation(context.response().toString());
            log.info("END - {}.{} request: {} hasItem: {} response: {} timelapse: {} ms", serviceName, operationName,
                    maskedRequest, getItemResponse.hasItem(), maskedResponse, elapsed);
        } else if (context.response() instanceof PutItemResponse) {
            log.info("END - {}.{} request: {} timelapse: {} ms", serviceName, operationName, maskedRequest, elapsed);
        } else if (context.response() instanceof DeleteItemResponse) {
            log.info("END - {}.{} request: {} timelapse: {} ms", serviceName, operationName, maskedRequest, elapsed);
        } else if (context.response() instanceof BatchGetItemResponse) {
            BatchGetItemResponse batchGetItemResponse = (BatchGetItemResponse) context.response();
            log.info("END - {}.{} request: {} hasUnprocessedKeys: {} timelapse: {} ms", serviceName, operationName,
                    maskedRequest, batchGetItemResponse.hasUnprocessedKeys(), elapsed);
        } else if (context.response() instanceof BatchWriteItemResponse) {
            BatchWriteItemResponse batchWriteItemResponse = (BatchWriteItemResponse) context.response();
            log.info("END - {}.{} request: {} hasUnprocessedItems: {} timelapse: {} ms", serviceName, operationName,
                    maskedRequest, batchWriteItemResponse.hasUnprocessedItems(), elapsed);
        }
    }

    @Override
    public void onExecutionFailure(Context.FailedExecution context, ExecutionAttributes executionAttributes) {
        log.warn("{}.{}", executionAttributes.getAttributes().get(new ExecutionAttribute<>(SERVICE_NAME)),
                executionAttributes.getAttributes().get(new ExecutionAttribute<>(OPERATION_NAME)),
                context.exception());
    }

}
