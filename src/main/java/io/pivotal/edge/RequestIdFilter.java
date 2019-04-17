package io.pivotal.edge;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
public class RequestIdFilter extends ZuulFilter {

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

        log.info("running request id filter");

        RequestContext ctx = RequestContext.getCurrentContext();
        String requestId = UUID.randomUUID().toString();
        ctx.addZuulRequestHeader("x-request-id", requestId);
        ctx.addZuulResponseHeader("x-request-id", requestId);
        return null;
    }

}
