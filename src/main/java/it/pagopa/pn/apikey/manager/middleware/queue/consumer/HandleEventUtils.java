package it.pagopa.pn.apikey.manager.middleware.queue.consumer;

import lombok.CustomLog;
import org.springframework.messaging.MessageHeaders;

@CustomLog
public class HandleEventUtils {
    private static final String PUBLIC_KEY_TABLE_STREAM_EVENT = "PUBLIC_KEY_TABLE_STREAM_EVENT - ";

    private HandleEventUtils() {
    }

    public static void handleException(MessageHeaders headers, Throwable t) {
        if (headers != null) {
            log.error(PUBLIC_KEY_TABLE_STREAM_EVENT + "Generic exception ex= {}", t.getMessage(), t);
        } else {
            log.error(PUBLIC_KEY_TABLE_STREAM_EVENT + "Generic exception ex ", t);
        }
    }
}
