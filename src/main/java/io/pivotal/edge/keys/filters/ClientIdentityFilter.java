package io.pivotal.edge.keys.filters;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import com.netflix.zuul.util.HTTPRequestUtils;
import io.pivotal.edge.EdgeRequestContext;
import io.pivotal.edge.events.EventPublisher;
import io.pivotal.edge.keys.ClientKeyService;
import io.pivotal.edge.keys.web.ClientKey;
import io.pivotal.edge.keys.web.ClientService;
import io.pivotal.edge.security.SecurityException;
import io.pivotal.edge.events.ClientIdentifiedEvent;
import io.pivotal.edge.servlet.filters.EdgeHttpServletRequestWrapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static io.pivotal.edge.EdgeApplicationConstants.*;
import static org.springframework.cloud.netflix.zuul.filters.support.FilterConstants.PRE_TYPE;

@Component
@Slf4j
public class ClientIdentityFilter extends ZuulFilter {

    private ClientIdentityService clientIdentityService;

    private ClientKeyService clientKeyService;

    private EventPublisher eventPublisher;

    public ClientIdentityFilter(ClientIdentityService clientIdentityService, ClientKeyService clientKeyService, EventPublisher eventPublisher) {
        this.clientIdentityService = clientIdentityService;
        this.clientKeyService = clientKeyService;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public String filterType() {
        return PRE_TYPE;
    }

    @Override
    public int filterOrder() {
        return 105;
    }

    @Override
    public boolean shouldFilter() {
        return true;
    }

    @Override
    public Object run() throws ZuulException {

        log.info("Executing Client Identity Filter");

        RequestContext ctx = RequestContext.getCurrentContext();

        EdgeRequestContext edgeRequestContext = clientIdentityService.createEdgeRequestContextFrom(ctx);
        if (StringUtils.isEmpty(edgeRequestContext.getClientId())) {
            log.info("Client Identity Filter: Client Credentials could not be resolved");
            throw new ZuulException(new SecurityException(), HttpStatus.FORBIDDEN.value(), "Invalid Client Credentials");
        }

        ClientKey clientKey = clientKeyService.findById(edgeRequestContext.getClientId());
        if (Objects.isNull(clientKey)) {
            log.info("Client Identity Filter: Client Credentials could not be resolved");
            throw new ZuulException(new SecurityException(), HttpStatus.FORBIDDEN.value(), "Invalid Client Credentials");
        }
        this.updateContextFromClientKey(edgeRequestContext, clientKey);

        eventPublisher.publishEvent(ClientIdentifiedEvent.builder()
                .requestId(ctx.getZuulRequestHeaders().get(REQUEST_ID_HEADER_NAME))
                .clientKey(edgeRequestContext.getClientId())
                .request(ctx.getRequest())
                .build());

        ctx.set(EDGE_REQUEST_CONTEXT, edgeRequestContext);
        this.stripClientKeyFrom(ctx, edgeRequestContext);

        return null;
    }

    private void updateContextFromClientKey(EdgeRequestContext edgeRequestContext, ClientKey clientKey) {
        List<ClientService> services = clientKey.getServices();
        edgeRequestContext.setApplicationType(clientKey.getApplicationType().name());
        edgeRequestContext.setClientSecretKey(clientKey.getSecretKey());
        if (Objects.nonNull(services)) {
            Map<String,String> allowedServices = new HashMap<>();
            services.forEach(c -> allowedServices.put(c.getId(), c.getPath()));
            edgeRequestContext.setAllowedServices(allowedServices);
        }
    }

    private void stripClientKeyFrom(RequestContext requestContext, EdgeRequestContext clientCreds) {

        Map<String, List<String>> queryParams = HTTPRequestUtils.getInstance().getQueryParams();
        if (Objects.nonNull(queryParams)) {
            queryParams.remove(API_KEY_PARAM);
            requestContext.setRequestQueryParams(queryParams);
        }

        if (StringUtils.equalsIgnoreCase(clientCreds.getRealm(), "BASIC")) {
            EdgeHttpServletRequestWrapper edgeRequestWrapper = EdgeHttpServletRequestWrapper.extractFrom(requestContext.getRequest());
            if (Objects.nonNull(edgeRequestWrapper)) {
                edgeRequestWrapper.remove(HttpHeaders.AUTHORIZATION);
            }
        }
    }

    private String getRequestUriFrom(RequestContext ctx) {
        HttpServletRequest request = ctx.getRequest();
        return request == null ? null : request.getRequestURI();
    }
}
