package io.pivotal.edge.auditing;

import com.netflix.zuul.http.HttpServletRequestWrapper;
import io.pivotal.edge.security.SecurityVerifiedEvent;
import io.pivotal.edge.servlet.filters.EdgeHttpServletRequestWrapper;
import io.pivotal.edge.events.OriginRequestCompletedEvent;
import io.pivotal.edge.events.RequestCompletedEvent;
import io.pivotal.edge.events.RequestInitiatedEvent;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.Header;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.netflix.zuul.filters.Route;
import org.springframework.cloud.netflix.zuul.filters.RouteLocator;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
@Slf4j
public class AuditingService {

    private AuditLogRecordRepository auditLogRecordRepository;

    private RouteLocator routeLocator;

    public AuditingService(AuditLogRecordRepository auditLogRecordRepository, RouteLocator routeLocator) {
        this.auditLogRecordRepository = auditLogRecordRepository;
        this.routeLocator = routeLocator;
    }

    public Collection<AuditLogRecord> getAuditLogRecords() {
        return auditLogRecordRepository.findAll();
    }

    public void createAuditLogRecordFrom(RequestInitiatedEvent requestEvent) {

        log.info("Creating audit log record");

        EdgeHttpServletRequestWrapper httpServletRequest = requestEvent.getHttpServletRequest();
        String requestUri = httpServletRequest.getRequestURI();
        Route matchingRoute = routeLocator.getMatchingRoute(requestUri);

        if (Objects.nonNull(matchingRoute)) {
            AuditLogRecord record = new AuditLogRecord();
            record.setServiceId(matchingRoute.getId());
            record.setRequestDate(requestEvent.getInitiatedTime());
            record.setMethod(HttpMethod.resolve(httpServletRequest.getMethod()));
            record.setRequestUri(requestUri);

            auditLogRecordRepository.save(record);
            httpServletRequest.setRequestId(record.getId());
        }
    }

    public void updateAuditLogRecordForPostOriginFrom(OriginRequestCompletedEvent requestEvent) {

        log.info("Updating audit log record");

        Header requestIdHeader = requestEvent.getHttpRequest().getFirstHeader("x-request-id");
        AuditLogRecord logRecord = (requestIdHeader == null ? null : auditLogRecordRepository.findById(requestIdHeader.getValue()));
        if (Objects.nonNull(logRecord)) {
            logRecord.setOriginExecutionTimeMillis(ChronoUnit.MILLIS.between(requestEvent.getStartTime(), requestEvent.getEndTime()));
            logRecord.setOriginHttpStatus(requestEvent.getHttpResponse().getStatusLine().getStatusCode());
        }
    }

    public void updateAuditLogRecordForSecurityVerifiedFrom(SecurityVerifiedEvent requestEvent) {

        AuditLogRecord auditLogRecord = this.getAuditLogRecordFor(requestEvent.getRequest());
        if (Objects.nonNull(auditLogRecord)) {
            auditLogRecord.setClientKey(requestEvent.getClientKey());
        }
    }

    public void finalizeAuditLogRecordFrom(RequestCompletedEvent requestEvent) {

        log.info("Finalizing audit log record");

        HttpServletResponse httpServletResponse = requestEvent.getHttpServletResponse();
        AuditLogRecord logRecord = auditLogRecordRepository.findById(requestEvent.getHttpServletRequest().getRequestId());
        if (Objects.nonNull(logRecord)) {
            logRecord.setExecutionTimeMillis(ChronoUnit.MILLIS.between(requestEvent.getStartTime(), requestEvent.getEndTime()));
            logRecord.setHttpStatus(httpServletResponse.getStatus());
        } else {
            log.info("Finalizing audit log record failed due to record not found");
        }
    }

    public AuditLogRecord getAuditLogRecordFor(HttpServletRequest request) {
        AuditLogRecord auditLogRecord = null;
        if (request instanceof HttpServletRequestWrapper) {
            HttpServletRequestWrapper requestWrapper = (HttpServletRequestWrapper)request;
            EdgeHttpServletRequestWrapper edgeRequestWrapper = (EdgeHttpServletRequestWrapper)requestWrapper.getRequest();
            auditLogRecord = auditLogRecordRepository.findById(edgeRequestWrapper.getRequestId());
        }
        return auditLogRecord;
    }
}
