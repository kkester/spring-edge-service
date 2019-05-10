package io.pivotal.edge.routing;

import io.pivotal.edge.events.EventPublisher;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

public class EdgeServiceHttpClientBuilder extends HttpClientBuilder {

    private EventPublisher eventPublisher;

    public static HttpClientBuilder create(EventPublisher eventPublisher) {
        return new EdgeServiceHttpClientBuilder(eventPublisher);
    }

    protected EdgeServiceHttpClientBuilder(EventPublisher eventPublisher) {
        super();
        this.eventPublisher = eventPublisher;
    }

    public CloseableHttpClient build() {

        /*CacheConfig cacheConfig = CacheConfig.custom()
                .setMaxCacheEntries(1000)
                .setMaxObjectSize(8192)
                .build();

        CloseableHttpClient cachingClient = CachingHttpClients.custom()
                .setCacheConfig(cacheConfig)
                .build();*/

        return new CloseableHttpClientWrapper(super.build(), eventPublisher);
    }

}
