package it.pagopa.pn.apikey.manager.log;

import it.pagopa.pn.apikey.manager.utils.MaskDataUtils;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.core.interceptor.*;
import software.amazon.awssdk.services.dynamodb.model.*;

@Slf4j
public class AwsClientLoggerInterceptor implements ExecutionInterceptor {

    private static final ExecutionAttribute<String> SERVICE_NAME = SdkExecutionAttribute.SERVICE_NAME;
    private static final ExecutionAttribute<String> OPERATION_NAME = SdkExecutionAttribute.OPERATION_NAME;
    private static final ExecutionAttribute<Long> START_TIME = new ExecutionAttribute<>("startTime");

    @Override
    public void beforeExecution(Context.BeforeExecution context, ExecutionAttributes executionAttributes) {
        log.info("START - {}.{} {}", executionAttributes.getAttributes().get(SERVICE_NAME),
                executionAttributes.getAttributes().get(OPERATION_NAME),
                MaskDataUtils.maskInformation(context.request().toString()));
        executionAttributes.putAttribute(START_TIME, System.currentTimeMillis());
    }

    @Override
    public void afterExecution(Context.AfterExecution context, ExecutionAttributes executionAttributes) {
        Long startTime = executionAttributes.getAttribute(START_TIME);
        Long elapsed = startTime != null ? System.currentTimeMillis() - startTime : null;

        Object serviceName = executionAttributes.getAttributes().get(SERVICE_NAME);
        Object operationName = executionAttributes.getAttributes().get(OPERATION_NAME);

        String maskedRequest = MaskDataUtils.maskInformation(context.request().toString());
        if (context.response() instanceof ScanResponse response) {
            log.info("END - {}.{} request: {} count: {} timelapse: {} ms", serviceName, operationName, maskedRequest,
                    response.count(), elapsed);
        } else if (context.response() instanceof QueryResponse response) {
            log.info("END - {}.{} request: {} count: {} timelapse: {} ms", serviceName, operationName, maskedRequest,
                    response.count(), elapsed);
        } else if (context.response() instanceof GetItemResponse response) {
            String maskedResponse = MaskDataUtils.maskInformation(context.response().toString());
            log.info("END - {}.{} request: {} hasItem: {} response: {} timelapse: {} ms", serviceName, operationName,
                    maskedRequest, response.hasItem(), maskedResponse, elapsed);
        } else if (context.response() instanceof PutItemResponse) {
            log.info("END - {}.{} request: {} timelapse: {} ms", serviceName, operationName, maskedRequest, elapsed);
        } else if (context.response() instanceof DeleteItemResponse) {
            log.info("END - {}.{} request: {} timelapse: {} ms", serviceName, operationName, maskedRequest, elapsed);
        } else if (context.response() instanceof BatchGetItemResponse response) {
            log.info("END - {}.{} request: {} hasUnprocessedKeys: {} timelapse: {} ms", serviceName, operationName,
                    maskedRequest, response.hasUnprocessedKeys(), elapsed);
        } else if (context.response() instanceof BatchWriteItemResponse response) {
            log.info("END - {}.{} request: {} hasUnprocessedItems: {} timelapse: {} ms", serviceName, operationName,
                    maskedRequest, response.hasUnprocessedItems(), elapsed);
        }
    }

    @Override
    public void onExecutionFailure(Context.FailedExecution context, ExecutionAttributes executionAttributes) {
        log.warn("{}.{}", executionAttributes.getAttributes().get(SERVICE_NAME),
                executionAttributes.getAttributes().get(OPERATION_NAME),
                context.exception());
    }

}
