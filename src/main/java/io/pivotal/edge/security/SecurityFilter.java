package io.pivotal.edge.security;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import io.pivotal.edge.routing.EdgeRequestContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.cloud.netflix.zuul.filters.Route;
import org.springframework.cloud.netflix.zuul.filters.RouteLocator;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.Objects;

import static io.pivotal.edge.EdgeApplicationConstants.EDGE_REQUEST_CONTEXT;
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
        Route route = routeLocator.getMatchingRoute(this.getRequestUriFrom(ctx));
        if (Objects.isNull(route)) {
            return null;
        }

        EdgeRequestContext edgeRequestContext = (EdgeRequestContext)ctx.get(EDGE_REQUEST_CONTEXT);
        if (Objects.isNull(edgeRequestContext) || StringUtils.isEmpty(edgeRequestContext.getClientId())) {
            log.info("Security Filter: Client Credentials could not be resolved");
            throw new ZuulException(new SecurityException(), HttpStatus.FORBIDDEN.value(), "Invalid Client Credentials");
        }

        edgeRequestContext.setServiceId(route.getId());
        if (!securityService.validate(edgeRequestContext)) {
            log.info("Security Filter: Client Key could not be resolved for credentials and service id");
            throw new ZuulException(new SecurityException(), HttpStatus.FORBIDDEN.value(), "Invalid Client Credentials");
        };

        return null;
    }



    private String getRequestUriFrom(RequestContext ctx) {
        HttpServletRequest request = ctx.getRequest();
        return request == null ? null : request.getRequestURI();
    }
}
