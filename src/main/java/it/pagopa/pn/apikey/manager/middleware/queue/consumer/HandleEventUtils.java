package it.pagopa.pn.apikey.manager.middleware.queue.consumer;

import lombok.CustomLog;
import org.springframework.messaging.MessageHeaders;

@CustomLog
public class HandleEventUtils {
    private static final String APIKEY_MANAGER_HANDLE_EXCEPTION = "APIKEY MANAGER - Handle exception - ";

    private HandleEventUtils() {
    }

    public static void handleException(MessageHeaders headers, Throwable t) {
        if (headers != null) {
            log.error(APIKEY_MANAGER_HANDLE_EXCEPTION + "Generic exception ex= {}", t.getMessage(), t);
        } else {
            log.error(APIKEY_MANAGER_HANDLE_EXCEPTION + "Generic exception ex ", t);
        }
    }
}
