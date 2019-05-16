package io.pivotal.edge.routing;

import io.pivotal.edge.events.EventPublisher;
import io.pivotal.edge.events.OriginRequestCompletedEvent;
import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.client.cache.HttpCacheContext;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HttpContext;

import java.io.IOException;
import java.time.LocalDateTime;

import static io.pivotal.edge.EdgeApplicationConstants.REQUEST_ID_HEADER_NAME;

public class CloseableHttpClientWrapper extends CloseableHttpClient {

    private CloseableHttpClient closeableHttpClient;

    private EventPublisher eventPublisher;

    public CloseableHttpClientWrapper(CloseableHttpClient closeableHttpClient, EventPublisher eventPublisher) {
        this.closeableHttpClient = closeableHttpClient;
        this.eventPublisher = eventPublisher;
    }

    @Override
    protected CloseableHttpResponse doExecute(HttpHost target, HttpRequest request, HttpContext context) throws IOException {

        HttpCacheContext httpCacheContext = HttpCacheContext.create();
        LocalDateTime now = LocalDateTime.now();
        CloseableHttpResponse httpResponse = null;
        try {
            httpResponse = closeableHttpClient.execute(target, request, httpCacheContext);
        } finally {
            LocalDateTime endTime = LocalDateTime.now();
            Header requestIdHeader = request.getFirstHeader(REQUEST_ID_HEADER_NAME);
            OriginRequestCompletedEvent requestCompletedEvent = OriginRequestCompletedEvent.builder()
                    .endTime(endTime)
                    .requestId(requestIdHeader == null ? null : requestIdHeader.getValue())
                    .host(target)
                    .context(httpCacheContext)
                    .httpRequest(request)
                    .httpResponse(httpResponse)
                    .startTime(now)
                    .build();
            eventPublisher.publishEvent(requestCompletedEvent);
        }
        return httpResponse;
    }

    @Override
    public void close() throws IOException {
        closeableHttpClient.close();
    }

    @Override
    public HttpParams getParams() {
        return closeableHttpClient.getParams();
    }

    @Override
    public ClientConnectionManager getConnectionManager() {
        return closeableHttpClient.getConnectionManager();
    }
}
