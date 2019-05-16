package io.pivotal.edge.auditing;

import io.pivotal.edge.events.ClientIdentifiedEvent;
import io.pivotal.edge.events.OriginRequestCompletedEvent;
import io.pivotal.edge.events.RequestCompletedEvent;
import io.pivotal.edge.events.RequestInitiatedEvent;
import io.pivotal.edge.servlet.filters.EdgeHttpServletRequestWrapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpResponse;
import org.apache.http.client.cache.CacheResponseStatus;
import org.springframework.cloud.netflix.zuul.filters.Route;
import org.springframework.cloud.netflix.zuul.filters.RouteLocator;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.UUID;

import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE;
import static java.time.temporal.ChronoField.*;

@Service
@Slf4j
public class AuditingService {

    private static final DateTimeFormatter DATE_FORMAT = new DateTimeFormatterBuilder()
            .parseCaseInsensitive()
            .append(ISO_LOCAL_DATE)
            .appendLiteral('T')
            .appendValue(HOUR_OF_DAY, 2)
            .appendLiteral(':')
            .appendValue(MINUTE_OF_HOUR, 2)
            .appendLiteral(':')
            .appendValue(SECOND_OF_MINUTE, 2)
            .toFormatter();

    private AuditLogRecordCache auditLogRecordCache;

    private RouteLocator routeLocator;

    public AuditingService(AuditLogRecordCache auditLogRecordCache, RouteLocator routeLocator) {
        this.auditLogRecordCache = auditLogRecordCache;
        this.routeLocator = routeLocator;
    }

    public void createAuditLogRecordFrom(RequestInitiatedEvent requestEvent) {

        log.info("Creating audit log record");

        EdgeHttpServletRequestWrapper httpServletRequest = requestEvent.getHttpServletRequest();
        String requestUri = httpServletRequest.getRequestURI();
        Route matchingRoute = routeLocator.getMatchingRoute(requestUri);

        if (Objects.nonNull(matchingRoute)) {
            String requestId = UUID.randomUUID().toString();
            AuditLogRecord record = new AuditLogRecord();
            record.setId(requestId);
            record.setServiceId(matchingRoute.getId());
            record.setRequestDate(DATE_FORMAT.format(requestEvent.getInitiatedTime()));
            record.setMethod(HttpMethod.resolve(httpServletRequest.getMethod()));
            record.setRequestUri(requestUri);
            record.setHost(httpServletRequest.getRemoteHost() + ":" + httpServletRequest.getServerPort());
            httpServletRequest.setRequestId(requestId);
            auditLogRecordCache.cache(record);
        }
    }

    public void updateAuditLogRecordForPostOriginFrom(OriginRequestCompletedEvent requestEvent) {

        log.info("Updating audit log record");

        AuditLogRecord logRecord = auditLogRecordCache.findById(requestEvent.getRequestId());
        if (Objects.isNull(logRecord)) {
            return;
        }

        logRecord.setOriginExecutionTimeMillis(ChronoUnit.MILLIS.between(requestEvent.getStartTime(), requestEvent.getEndTime()));
        logRecord.setOriginHost(requestEvent.getHost().toHostString());
        HttpResponse httpResponse = requestEvent.getHttpResponse();
        if (Objects.nonNull(httpResponse)) {
            logRecord.setOriginHttpStatus(httpResponse.getStatusLine().getStatusCode());
        }
        CacheResponseStatus cacheResponseStatus = requestEvent.getContext().getCacheResponseStatus();
        if (Objects.nonNull(cacheResponseStatus)) {
            logRecord.setCacheStatus(cacheResponseStatus.name());
        } else {
            logRecord.setCacheStatus("NONE");
        }
    }

    public void updateAuditLogRecordForSecurityVerifiedFrom(ClientIdentifiedEvent requestEvent) {

        AuditLogRecord auditLogRecord = auditLogRecordCache.findById(requestEvent.getRequestId());
        if (Objects.nonNull(auditLogRecord)) {
            auditLogRecord.setClientKey(requestEvent.getClientKey());
        }
    }

    public void finalizeAuditLogRecordFrom(RequestCompletedEvent requestEvent) {

        log.info("Finalizing audit log record");

        HttpServletResponse httpServletResponse = requestEvent.getHttpServletResponse();
        AuditLogRecord logRecord = auditLogRecordCache.findById(requestEvent.getHttpServletRequest().getRequestId());
        if (Objects.nonNull(logRecord)) {
            logRecord.setExecutionTimeMillis(ChronoUnit.MILLIS.between(requestEvent.getStartTime(), requestEvent.getEndTime()));
            logRecord.setHttpStatus(httpServletResponse.getStatus());
            auditLogRecordCache.save(logRecord);
        } else {
            log.info("Finalizing audit log record failed due to record not found");
        }
    }

    public AuditLogRecord getAuditLogRecordById(String requestId) {
        return auditLogRecordCache.findById(requestId);
    }

}
