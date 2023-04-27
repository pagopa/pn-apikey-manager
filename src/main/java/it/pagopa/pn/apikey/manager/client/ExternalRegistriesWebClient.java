package it.pagopa.pn.apikey.manager.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;

import java.time.Duration;

@Component
@Slf4j
public class ExternalRegistriesWebClient extends CommonWebClient {

    private final Integer tcpMaxPoolSize;
    private final Integer tcpMaxQueuedConnections;
    private final Integer tcpPendingAcquireTimeout;
    private final Integer tcpPoolIdleTimeout;
    private final String basePath;

    public ExternalRegistriesWebClient(@Value("${pn.apikey.manager.webclient.pn-external-registries.tcp-max-poolsize}") Integer tcpMaxPoolSize,
                                       @Value("${pn.apikey.manager.webclient.pn-external-registries.tcp-max-queued-connections}") Integer tcpMaxQueuedConnections,
                                       @Value("${pn.apikey.manager.webclient.pn-external-registries.tcp-pending-acquired-timeout}") Integer tcpPendingAcquireTimeout,
                                       @Value("${pn.apikey.manager.webclient.pn-external-registries.tcp-pool-idle-timeout}") Integer tcpPoolIdleTimeout,
                                       @Value("${pn.apikey.manager.pn-external-registries.base-path}") String basePath) {
        this.tcpMaxPoolSize = tcpMaxPoolSize;
        this.tcpMaxQueuedConnections = tcpMaxQueuedConnections;
        this.tcpPendingAcquireTimeout = tcpPendingAcquireTimeout;
        this.tcpPoolIdleTimeout = tcpPoolIdleTimeout;
        this.basePath = basePath;
    }

    public WebClient init() {
        ConnectionProvider provider = ConnectionProvider.builder("fixed")
                .maxConnections(tcpMaxPoolSize)
                .pendingAcquireMaxCount(tcpMaxQueuedConnections)
                .pendingAcquireTimeout(Duration.ofMillis(tcpPendingAcquireTimeout))
                .maxIdleTime(Duration.ofMillis(tcpPoolIdleTimeout)).build();

        HttpClient httpClient = HttpClient.create(provider);

        return super.initWebClient(httpClient, basePath);
    }
}
