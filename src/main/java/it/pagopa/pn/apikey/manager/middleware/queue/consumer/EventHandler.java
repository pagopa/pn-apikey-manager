package it.pagopa.pn.apikey.manager.middleware.queue.consumer;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

@Data
@Component
@ConfigurationProperties(prefix = "pn.apikey.manager.event")
public class EventHandler {
    private Map<String, String> handler;
}
