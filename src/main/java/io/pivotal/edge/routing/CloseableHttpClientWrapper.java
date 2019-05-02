package io.pivotal.edge.routing;

import io.pivotal.edge.events.EventPublisher;
import io.pivotal.edge.events.OriginRequestCompletedEvent;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HttpContext;

import java.io.IOException;
import java.time.LocalDateTime;

public class CloseableHttpClientWrapper extends CloseableHttpClient {

    private CloseableHttpClient closeableHttpClient;

    private EventPublisher eventPublisher;

    public CloseableHttpClientWrapper(CloseableHttpClient closeableHttpClient, EventPublisher eventPublisher) {
        this.closeableHttpClient = closeableHttpClient;
        this.eventPublisher = eventPublisher;
    }

    @Override
    protected CloseableHttpResponse doExecute(HttpHost target, HttpRequest request, HttpContext context) throws IOException {

        LocalDateTime now = LocalDateTime.now();
        CloseableHttpResponse httpResponse = closeableHttpClient.execute(target, request, context);
        eventPublisher.publishEvent(OriginRequestCompletedEvent.builder().httpRequest(request).httpResponse(httpResponse).startTime(now).endTime(LocalDateTime.now()).build());
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
