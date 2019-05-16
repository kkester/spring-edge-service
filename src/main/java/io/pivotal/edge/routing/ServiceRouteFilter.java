package io.pivotal.edge.routing;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import io.pivotal.edge.EdgeRequestContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Objects;

import static io.pivotal.edge.EdgeApplicationConstants.EDGE_REQUEST_CONTEXT;
import static org.springframework.cloud.netflix.zuul.filters.support.FilterConstants.ROUTE_TYPE;

@Component
@Slf4j
public class ServiceRouteFilter extends ZuulFilter {

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
        EdgeRequestContext edgeRequestContext = (EdgeRequestContext) ctx.get(EDGE_REQUEST_CONTEXT);
        if (Objects.isNull(edgeRequestContext)) {
            return null;
        }

        String serviceId = edgeRequestContext.getServiceId();
        String servicePath = edgeRequestContext.getAllowedServices().get(serviceId);
        if (StringUtils.isNotBlank(servicePath)) {
            log.debug("Applying Service Path Override Executing Service Route Filter");
            try {
                ctx.setRouteHost(new URL(servicePath));
            } catch (MalformedURLException e) {
                log.warn("Error occurred applying service configured host to route");
            }
        }

        return null;
    }
}
