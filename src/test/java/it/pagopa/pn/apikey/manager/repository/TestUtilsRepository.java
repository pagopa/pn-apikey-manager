package it.pagopa.pn.apikey.manager.repository;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import software.amazon.awssdk.core.async.SdkPublisher;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbAsyncTable;
import software.amazon.awssdk.enhanced.dynamodb.model.Page;
import software.amazon.awssdk.enhanced.dynamodb.model.PagePublisher;
import software.amazon.awssdk.enhanced.dynamodb.model.ScanEnhancedRequest;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class TestUtilsRepository {
    public static <T> void mockScanEnhancedRequestToRetrievePage(DynamoDbAsyncTable<T> dynamoDbAsyncTable, List<T> entities) {
        SdkPublisher<Page<T>> sdkPublisher = mock(SdkPublisher.class);
        doAnswer(invocation -> {
            Page<T> page = Page.create(entities, null);

            Subscriber<? super Page<T>> subscriber = invocation.getArgument(0);
            subscriber.onSubscribe(new Subscription() {
                @Override
                public void request(long n) {
                    if (n != 0) {
                        subscriber.onNext(page);
                        subscriber.onComplete();
                    }
                }

                @Override
                public void cancel() { }
            });
            return null;
        }).when(sdkPublisher).subscribe((Subscriber<? super Page<T>>) any());

        PagePublisher<T> pagePublisher = PagePublisher.create(sdkPublisher);
        when(dynamoDbAsyncTable.scan((ScanEnhancedRequest) any())).thenReturn(pagePublisher);
    }
}
