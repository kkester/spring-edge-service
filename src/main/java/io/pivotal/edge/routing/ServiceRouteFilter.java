package io.pivotal.edge.routing;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import io.pivotal.edge.keys.ClientService;
import io.pivotal.edge.security.ClientSecretCredentials;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.cloud.netflix.zuul.filters.Route;
import org.springframework.stereotype.Component;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Objects;

import static io.pivotal.edge.EdgeApplicationConstants.ROUTE;
import static org.springframework.cloud.netflix.zuul.filters.support.FilterConstants.ROUTE_TYPE;

@Component
@Slf4j
public class ServiceRouteFilter extends ZuulFilter {

    private ClientRoutingService clientRoutingService;

    public ServiceRouteFilter(ClientRoutingService clientRoutingService) {
        this.clientRoutingService = clientRoutingService;
    }

    @Override
    public String filterType() {
        return ROUTE_TYPE;
    }

    @Override
    public int filterOrder() {
        return 90;
    }

    @Override
    public boolean shouldFilter() {
        return true;
    }

    @Override
    public Object run() {

        log.info("Executing Service Route Filter");

        RequestContext ctx = RequestContext.getCurrentContext();
        Route route = (Route)ctx.get(ROUTE);
        if (Objects.isNull(route)) {
            return null;
        }

        ClientSecretCredentials clientCreds = ClientSecretCredentials.createFrom(ctx);
        ClientService clientService = clientRoutingService.getClientServiceWithServiceId(clientCreds, route.getId());
        if (clientService != null) {
            if (StringUtils.isNotBlank(clientService.getPath())) {
                log.debug("Applying Service Path Override Executing Service Route Filter");
                try {
                    ctx.setRouteHost(new URL(clientService.getPath()));
                } catch (MalformedURLException e) {
                    log.warn("Error occurred applying service configured host to route");
                }
            }
        }

        return null;
    }
}
