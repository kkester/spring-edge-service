package io.pivotal.edge.security;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import io.pivotal.edge.events.EventPublisher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.Objects;

import static org.springframework.cloud.netflix.zuul.filters.support.FilterConstants.ERROR_TYPE;

@Component
@Slf4j
public class SecurityErrorFilter extends ZuulFilter {

    private EventPublisher eventPublisher;

    public SecurityErrorFilter(EventPublisher eventPublisher) {
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
        ClientSecretCredentials clientCreds = ClientSecretCredentials.createFrom(ctx);
        if (Objects.nonNull(clientCreds)) {
            eventPublisher.publishEvent(SecurityVerifiedEvent.builder().request(ctx.getRequest()).clientKey(clientCreds.getClientKey()).build());
        }

        return null;
    }

}
