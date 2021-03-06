package io.pivotal.edge.auditing;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import io.pivotal.edge.servlet.filters.EdgeHttpServletRequestWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.Objects;

import static io.pivotal.edge.EdgeApplicationConstants.REQUEST_ID_HEADER_NAME;
import static org.springframework.cloud.netflix.zuul.filters.support.FilterConstants.ERROR_TYPE;

@Slf4j
@Component
public class RequestIdErrorFilter extends ZuulFilter {

    @Autowired
    private AuditingService auditingService;

    @Override
    public String filterType() {
        return ERROR_TYPE;
    }

    @Override
    public int filterOrder() {
        return 200;
    }

    @Override
    public boolean shouldFilter() {
        return true;
    }

    @Override
    public Object run() {

        log.info("Executing Request Id Error Filter");

        RequestContext ctx = RequestContext.getCurrentContext();
        EdgeHttpServletRequestWrapper edgeRequestWrapper = EdgeHttpServletRequestWrapper.extractFrom(ctx.getRequest());
        String requestId = (Objects.isNull(edgeRequestWrapper) ? null : edgeRequestWrapper.getRequestId());
        AuditLogRecord auditLogRecord = auditingService.getAuditLogRecordById(requestId);
        if (!Objects.isNull(auditLogRecord)) {
            ctx.getResponse().addHeader(REQUEST_ID_HEADER_NAME, auditLogRecord.getId());
        }

        return null;
    }

}
