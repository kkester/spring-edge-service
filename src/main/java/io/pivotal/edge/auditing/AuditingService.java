package io.pivotal.edge.auditing;

import com.netflix.zuul.http.HttpServletRequestWrapper;
import io.pivotal.edge.events.OriginRequestCompletedEvent;
import io.pivotal.edge.events.RequestCompletedEvent;
import io.pivotal.edge.events.RequestInitiatedEvent;
import io.pivotal.edge.security.SecurityVerifiedEvent;
import io.pivotal.edge.servlet.filters.EdgeHttpServletRequestWrapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.Header;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.springframework.cloud.netflix.zuul.filters.Route;
import org.springframework.cloud.netflix.zuul.filters.RouteLocator;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import static io.pivotal.edge.EdgeApplicationConstants.REQUEST_ID_HEADER_NAME;
import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE;
import static java.time.temporal.ChronoField.*;

@Service
@Slf4j
public class AuditingService {

    private AuditLogRecordRepository auditLogRecordRepository;

    private RouteLocator routeLocator;

    private Map<String, AuditLogRecord> auditLogRecordCache = new HashMap<>();

    private DateTimeFormatter DATE_FORMAT = new DateTimeFormatterBuilder()
            .parseCaseInsensitive()
            .append(ISO_LOCAL_DATE)
            .appendLiteral('T')
            .appendValue(HOUR_OF_DAY, 2)
            .appendLiteral(':')
            .appendValue(MINUTE_OF_HOUR, 2)
            .appendLiteral(':')
            .appendValue(SECOND_OF_MINUTE, 2)
            .toFormatter();

    public AuditingService(AuditLogRecordRepository auditLogRecordRepository, RouteLocator routeLocator) {
        this.auditLogRecordRepository = auditLogRecordRepository;
        this.routeLocator = routeLocator;
    }

    public Iterable<AuditLogRecord> getAuditLogRecords() {
        return auditLogRecordRepository.findAll();
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
            record.setHost(httpServletRequest.getRemoteHost()+":"+httpServletRequest.getServerPort());
            httpServletRequest.setRequestId(requestId);
            this.cache(record);
        }
    }

    public void updateAuditLogRecordForPostOriginFrom(OriginRequestCompletedEvent requestEvent) {

        log.info("Updating audit log record");

        HttpRequest httpRequest = requestEvent.getHttpRequest();
        Header requestIdHeader = httpRequest.getFirstHeader(REQUEST_ID_HEADER_NAME);
        AuditLogRecord logRecord = this.getAuditLogRecordByIdFromCache(requestIdHeader.getValue());
        if (Objects.nonNull(logRecord)) {
            logRecord.setOriginExecutionTimeMillis(ChronoUnit.MILLIS.between(requestEvent.getStartTime(), requestEvent.getEndTime()));
            logRecord.setOriginHost(requestEvent.getHost().toHostString());
            HttpResponse httpResponse = requestEvent.getHttpResponse();
            if (Objects.nonNull(httpResponse)) {
                logRecord.setOriginHttpStatus(httpResponse.getStatusLine().getStatusCode());
            }
//            CacheResponseStatus cacheResponseStatus = requestEvent.getContext().getCacheResponseStatus();
//            if (Objects.nonNull(cacheResponseStatus)) {
//                logRecord.setCacheStatus(cacheResponseStatus.name());
//            }
            logRecord.setCacheStatus("NONE");
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
        AuditLogRecord logRecord = this.getAuditLogRecordByIdFromCache(requestEvent.getHttpServletRequest().getRequestId());
        if (Objects.nonNull(logRecord)) {
            logRecord.setExecutionTimeMillis(ChronoUnit.MILLIS.between(requestEvent.getStartTime(), requestEvent.getEndTime()));
            logRecord.setHttpStatus(httpServletResponse.getStatus());
            auditLogRecordRepository.save(logRecord);
            this.removeFromCache(logRecord);
        } else {
            log.info("Finalizing audit log record failed due to record not found");
        }
    }

    public AuditLogRecord getAuditLogRecordFor(HttpServletRequest request) {
        AuditLogRecord auditLogRecord = null;
        if (request instanceof HttpServletRequestWrapper) {
            HttpServletRequestWrapper requestWrapper = (HttpServletRequestWrapper)request;
            if (requestWrapper.getRequest() instanceof EdgeHttpServletRequestWrapper) {
                EdgeHttpServletRequestWrapper edgeRequestWrapper = (EdgeHttpServletRequestWrapper) requestWrapper.getRequest();
                auditLogRecord = this.getAuditLogRecordByIdFromCache(edgeRequestWrapper.getRequestId());
            }
        }
        return auditLogRecord;
    }

    private void cache(AuditLogRecord record) {
        auditLogRecordCache.put(record.getId(), record);
    }

    private AuditLogRecord getAuditLogRecordByIdFromCache(String requestId) {
        return auditLogRecordCache.get(requestId);
    }

    private void removeFromCache(AuditLogRecord logRecord) {
        auditLogRecordCache.remove(logRecord.getId());
    }
}
