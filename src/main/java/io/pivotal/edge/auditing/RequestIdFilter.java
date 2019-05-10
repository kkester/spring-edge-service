package io.pivotal.edge.auditing;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Objects;

import static io.pivotal.edge.EdgeApplicationConstants.REQUEST_ID_HEADER_NAME;
import static org.springframework.cloud.netflix.zuul.filters.support.FilterConstants.PRE_TYPE;

@Slf4j
@Component
public class RequestIdFilter extends ZuulFilter {

    private AuditingService auditingService;

    public RequestIdFilter(AuditingService auditingService) {
        this.auditingService = auditingService;
    }

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
        AuditLogRecord auditLogRecord = auditingService.getAuditLogRecordFor(ctx.getRequest());
        if (!Objects.isNull(auditLogRecord)) {
            ctx.addZuulRequestHeader(REQUEST_ID_HEADER_NAME, auditLogRecord.getId());
            ctx.addZuulResponseHeader(REQUEST_ID_HEADER_NAME, auditLogRecord.getId());
        }

        return null;
    }

}
