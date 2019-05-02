package io.pivotal.edge.limiting;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.netflix.zuul.filters.RouteLocator;
import org.springframework.stereotype.Component;

import java.util.UUID;

import static org.springframework.cloud.netflix.zuul.filters.support.FilterConstants.PRE_TYPE;

@Component
@Slf4j
public class RateLimitFilter extends ZuulFilter {

    @Autowired
    private RouteLocator routeLocator;

    @Override
    public String filterType() {
        return PRE_TYPE;
    }

    @Override
    public int filterOrder() {
        return 1;
    }

    @Override
    public boolean shouldFilter() {
        return true;
    }

    @Override
    public Object run() {

        log.info("Executing Rate Limit Filter");

        RequestContext ctx = RequestContext.getCurrentContext();
        routeLocator.getMatchingRoute(ctx.getRequest().getRequestURI());

        return null;
    }

}


