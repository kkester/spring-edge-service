package io.pivotal.edge.routing;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import io.pivotal.edge.keys.ClientService;
import io.pivotal.edge.security.ClientSecretCredentials;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.netflix.zuul.filters.Route;
import org.springframework.cloud.netflix.zuul.filters.RouteLocator;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

import java.net.MalformedURLException;
import java.net.URL;

import static org.springframework.cloud.netflix.zuul.filters.support.FilterConstants.ROUTE_TYPE;

@Component
@Slf4j
public class ServiceRouteFilter extends ZuulFilter {

    @Autowired
    private ClientRoutingService clientRoutingService;

    @Autowired
    private RouteLocator routeLocator;

    @Override
    public String filterType() {
        return ROUTE_TYPE;
    }

    @Override
    public int filterOrder() {
        return 0;
    }

    @Override
    public boolean shouldFilter() {
        return true;
    }

    @Override
    public Object run() {

        log.info("Executing Service Route Filter");

        RequestContext ctx = RequestContext.getCurrentContext();

        Route route = routeLocator.getMatchingRoute(ctx.getRequest().getRequestURI());
        ClientSecretCredentials clientCreds = ClientSecretCredentials.createFrom(ctx);
        ClientService clientService = clientRoutingService.getClientServiceWithServiceId(clientCreds, route.getId());
        if (clientService != null) {
            if (StringUtils.isNotBlank(clientService.getPath())) {
                log.info("Applying Service Path Override Executing Service Route Filter");
                try {
                    ctx.setRouteHost(new URL(clientService.getPath()));
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
            }
        }

        return null;
    }
}
