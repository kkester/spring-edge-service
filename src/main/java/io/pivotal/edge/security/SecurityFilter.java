package io.pivotal.edge.security;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import io.pivotal.edge.keys.ClientKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.netflix.zuul.filters.Route;
import org.springframework.cloud.netflix.zuul.filters.RouteLocator;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.Objects;

import static org.springframework.cloud.netflix.zuul.filters.support.FilterConstants.PRE_TYPE;

@Component
@Slf4j
public class SecurityFilter extends ZuulFilter {

    private SecurityService securityService;

    private RouteLocator routeLocator;

    public SecurityFilter(SecurityService securityService, RouteLocator routeLocator) {
        this.securityService = securityService;
        this.routeLocator = routeLocator;
    }

    @Override
    public String filterType() {
        return PRE_TYPE;
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
    public Object run() throws ZuulException {

        log.info("Executing Security Filter");

        RequestContext ctx = RequestContext.getCurrentContext();

        ClientSecretCredentials clientCreds = ClientSecretCredentials.createFrom(ctx);
        if (Objects.isNull(clientCreds)) {
            log.info("Security Filter: Client Credentials could not be resolved");
            throw new ZuulException(new SecurityException(), HttpStatus.FORBIDDEN.value(), "Invalid Client Credentials");
        }

        Route route = routeLocator.getMatchingRoute(this.getRequestUriFrom(ctx));
        if (!Objects.isNull(route)) {
            ClientKey clientKey = securityService.getClientKeyWithServiceId(clientCreds, route.getId());
            if (Objects.isNull(clientKey)) {
                log.info("Security Filter: Client Key could not be resolved for credentials");
                throw new ZuulException(new SecurityException(), HttpStatus.FORBIDDEN.value(), "Invalid Client Credentials");
            }
        }

        return null;
    }

    private String getRequestUriFrom(RequestContext ctx) {
        HttpServletRequest request = ctx.getRequest();
        return request == null ? null : request.getRequestURI();
    }
}
