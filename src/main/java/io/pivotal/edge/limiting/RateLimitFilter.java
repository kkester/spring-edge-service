package io.pivotal.edge.limiting;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.netflix.zuul.filters.RouteLocator;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
public class RateLimitFilter extends ZuulFilter {

    @Autowired
    private RouteLocator routeLocator;

    @Override
    public String filterType() {
        return "pre";
    }

    @Override
    public int filterOrder() {
        return 1110;
    }

    @Override
    public boolean shouldFilter() {
        return true;
    }

    @Override
    public Object run() {

        log.info("running rate limit filter");

        RequestContext ctx = RequestContext.getCurrentContext();
        routeLocator.getMatchingRoute(ctx.getRequest().getRequestURI());

        return null;
    }

}
