package io.pivotal.edge.auditing;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import io.pivotal.edge.servlet.filters.EdgeHttpServletRequestWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Objects;

import static io.pivotal.edge.EdgeApplicationConstants.REQUEST_ID_HEADER_NAME;
import static org.springframework.cloud.netflix.zuul.filters.support.FilterConstants.PRE_TYPE;

@Slf4j
@Component
public class RequestIdFilter extends ZuulFilter {

    @Override
    public String filterType() {
        return PRE_TYPE;
    }

    @Override
    public int filterOrder() {
        return 100;
    }

    @Override
    public boolean shouldFilter() {
        return true;
    }

    @Override
    public Object run() {

        log.info("Executing Request Id Filter");

        RequestContext ctx = RequestContext.getCurrentContext();
        EdgeHttpServletRequestWrapper edgeRequestWrapper = EdgeHttpServletRequestWrapper.extractFrom(ctx.getRequest());
        if (!Objects.isNull(edgeRequestWrapper)) {
            String requestId = edgeRequestWrapper.getRequestId();
            ctx.put(REQUEST_ID_HEADER_NAME, requestId);
            ctx.addZuulRequestHeader(REQUEST_ID_HEADER_NAME, requestId);
            ctx.addZuulResponseHeader(REQUEST_ID_HEADER_NAME, requestId);
        }

        return null;
    }

}
