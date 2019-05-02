package io.pivotal.edge.auditing;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.UUID;

import static org.springframework.cloud.netflix.zuul.filters.support.FilterConstants.PRE_TYPE;

@Slf4j
@Component
public class RequestIdFilter extends ZuulFilter {

    @Autowired
    private AuditingService auditingService;

    @Override
    public String filterType() {
        return PRE_TYPE;
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

        log.info("Executing Request Id Filter");

        RequestContext ctx = RequestContext.getCurrentContext();
        AuditLogRecord auditLogRecord = auditingService.getAuditLogRecordFor(ctx.getRequest());
        if (!Objects.isNull(auditLogRecord)) {
            ctx.addZuulRequestHeader("x-request-id", auditLogRecord.getId());
            ctx.addZuulResponseHeader("x-request-id", auditLogRecord.getId());
        }
        return null;
    }

}
