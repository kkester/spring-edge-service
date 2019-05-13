package io.pivotal.edge.routing;

import io.pivotal.edge.events.EventPublisher;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.cache.CacheConfig;
import org.apache.http.impl.client.cache.CachingHttpClients;
import org.springframework.cloud.commons.httpclient.ApacheHttpClientConnectionManagerFactory;
import org.springframework.cloud.netflix.zuul.filters.ZuulProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HttpClientConfig {

    @Bean
    public CloseableHttpClient closableHttpClient(EventPublisher eventPublisher, ApacheHttpClientConnectionManagerFactory connectionManagerFactory, ZuulProperties properties) {


        ZuulProperties.Host hostProperties = properties.getHost();
        CacheConfig cacheConfig = CacheConfig.custom()
                .setMaxCacheEntries(1000)
                .setMaxObjectSize(8192)
                .build();

        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectionRequestTimeout(hostProperties.getConnectionRequestTimeoutMillis())
                .setSocketTimeout(hostProperties.getSocketTimeoutMillis())
                .setConnectTimeout(hostProperties.getConnectTimeoutMillis())
                .setCookieSpec(CookieSpecs.IGNORE_COOKIES).build();

        HttpClientConnectionManager connectionManager = connectionManagerFactory.newConnectionManager(
                true,
                hostProperties.getMaxTotalConnections(),
                hostProperties.getMaxPerRouteConnections(),
                hostProperties.getTimeToLive(),
                hostProperties.getTimeUnit(), null);

        CloseableHttpClient cachingClient = CachingHttpClients.custom()
                .setCacheConfig(cacheConfig)
                .setConnectionManager(connectionManager)
                .setDefaultRequestConfig(requestConfig)
                .build();

        return new CloseableHttpClientWrapper(cachingClient, eventPublisher);
    }

}
