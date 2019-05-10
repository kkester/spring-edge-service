package io.pivotal.edge.security;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import com.netflix.zuul.util.HTTPRequestUtils;
import io.pivotal.edge.events.EventPublisher;
import io.pivotal.edge.keys.ClientKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.netflix.zuul.filters.Route;
import org.springframework.cloud.netflix.zuul.filters.RouteLocator;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static io.pivotal.edge.EdgeApplicationConstants.*;
import static org.springframework.cloud.netflix.zuul.filters.support.FilterConstants.PRE_TYPE;

@Component
@Slf4j
public class SecurityFilter extends ZuulFilter {

    private SecurityService securityService;

    private RouteLocator routeLocator;

    private EventPublisher eventPublisher;

    public SecurityFilter(SecurityService securityService, RouteLocator routeLocator, EventPublisher eventPublisher) {
        this.securityService = securityService;
        this.routeLocator = routeLocator;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public String filterType() {
        return PRE_TYPE;
    }

    @Override
    public int filterOrder() {
        return 110;
    }

    @Override
    public boolean shouldFilter() {
        return true;
    }

    @Override
    public Object run() throws ZuulException {

        log.info("Executing Security Filter");

        RequestContext ctx = RequestContext.getCurrentContext();
        ClientSecretCredentials clientCreds = ClientSecretCredentials.createFrom(ctx);
        if (Objects.isNull(clientCreds)) {
            log.info("Security Filter: Client Credentials could not be resolved");
            throw new ZuulException(new SecurityException(), HttpStatus.FORBIDDEN.value(), "Invalid Client Credentials");
        }

        eventPublisher.publishEvent(SecurityVerifiedEvent.builder().request(ctx.getRequest()).clientKey(clientCreds.getClientKey()).build());

        Route route = routeLocator.getMatchingRoute(this.getRequestUriFrom(ctx));
        if (!Objects.isNull(route)) {
            ClientKey clientKey = securityService.getClientKeyWithServiceId(clientCreds, route.getId());
            if (Objects.isNull(clientKey)) {
                log.info("Security Filter: Client Key could not be resolved for credentials and service id");
                throw new ZuulException(new SecurityException(), HttpStatus.FORBIDDEN.value(), "Invalid Client Credentials");
            }
            ctx.set(CLIENT_KEY, clientKey);
        }
        ctx.set(ROUTE, route);
        this.stripApiKeyFromQueryParameters(ctx);

        return null;
    }

    private void stripApiKeyFromQueryParameters(RequestContext requestContext) {

        Map<String, List<String>> queryParams = HTTPRequestUtils.getInstance().getQueryParams();
        if (Objects.nonNull(queryParams)) {
            queryParams.remove(API_KEY_PARAM);
            requestContext.setRequestQueryParams(queryParams);
        }
    }

    private String getRequestUriFrom(RequestContext ctx) {
        HttpServletRequest request = ctx.getRequest();
        return request == null ? null : request.getRequestURI();
    }
}
