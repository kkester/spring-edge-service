package io.pivotal.edge.keys.filters;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import io.pivotal.edge.routing.EdgeRequestContext;
import io.pivotal.edge.events.EventPublisher;
import io.pivotal.edge.events.ClientIdentifiedEvent;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;

import java.util.Objects;

import static io.pivotal.edge.EdgeApplicationConstants.EDGE_REQUEST_CONTEXT;
import static org.springframework.cloud.netflix.zuul.filters.support.FilterConstants.ERROR_TYPE;

@Component
@Slf4j
public class ClientIdentityErrorFilter extends ZuulFilter {

    private ClientIdentityService clientIdentityService;

    private EventPublisher eventPublisher;

    public ClientIdentityErrorFilter(ClientIdentityService clientIdentityService, EventPublisher eventPublisher) {
        this.clientIdentityService = clientIdentityService;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public String filterType() {
        return ERROR_TYPE;
    }

    @Override
    public int filterOrder() {
        return 210;
    }

    @Override
    public boolean shouldFilter() {
        return true;
    }

    @Override
    public Object run() {

        log.info("Executing Security Error Filter");

        RequestContext ctx = RequestContext.getCurrentContext();
        EdgeRequestContext edgeRequestContext = (EdgeRequestContext)ctx.get(EDGE_REQUEST_CONTEXT);
        if (Objects.isNull(edgeRequestContext)) {
            edgeRequestContext = this.clientIdentityService.createEdgeRequestContextFrom(ctx);
        }

        if (StringUtils.isNotBlank(edgeRequestContext.getClientId())) {
            eventPublisher.publishEvent(ClientIdentifiedEvent.builder()
                    .request(ctx.getRequest())
                    .requestId(edgeRequestContext.getRequestId())
                    .clientKey(edgeRequestContext.getClientId())
                    .build());
        }

        return null;
    }

}
