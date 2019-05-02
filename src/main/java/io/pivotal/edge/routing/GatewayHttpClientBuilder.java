package io.pivotal.edge.routing;

import io.pivotal.edge.events.EventPublisher;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

public class GatewayHttpClientBuilder extends HttpClientBuilder {

    private EventPublisher eventPublisher;

    public static HttpClientBuilder create(EventPublisher eventPublisher) {
        return new GatewayHttpClientBuilder(eventPublisher);
    }

    protected GatewayHttpClientBuilder(EventPublisher eventPublisher) {
        super();
        this.eventPublisher = eventPublisher;
    }

    public CloseableHttpClient build() {
        return new CloseableHttpClientWrapper(super.build(), eventPublisher);
    }

}
