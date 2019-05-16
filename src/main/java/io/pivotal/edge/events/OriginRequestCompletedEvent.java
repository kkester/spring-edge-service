package io.pivotal.edge.events;

import lombok.Builder;
import lombok.Value;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.cache.HttpCacheContext;

import java.time.LocalDateTime;

@Value
@Builder
public class OriginRequestCompletedEvent {

    private String requestId;
    private HttpHost host;
    private HttpRequest httpRequest;
    private HttpResponse httpResponse;
    private HttpCacheContext context;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

}
