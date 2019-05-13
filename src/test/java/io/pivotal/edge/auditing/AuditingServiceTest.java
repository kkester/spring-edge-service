package io.pivotal.edge.auditing;

import com.netflix.zuul.http.HttpServletRequestWrapper;
import io.pivotal.edge.events.RequestInitiatedEvent;
import io.pivotal.edge.servlet.filters.EdgeHttpServletRequestWrapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.cloud.netflix.zuul.filters.Route;
import org.springframework.cloud.netflix.zuul.filters.RouteLocator;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class AuditingServiceTest {

    /** CUT */
    private AuditingService subject;

    @Mock
    private AuditLogRecordCache auditLogRecordCache;

    @Mock
    private RouteLocator routeLocator;

    @Mock
    private HttpServletRequestWrapper zuulRequestWrapper;

    private EdgeHttpServletRequestWrapper edgeRequestWrapper;

    @Before
    public void setUp() {

        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        when(mockRequest.getRequestURI()).thenReturn("http://domain.net/resource");
        when(mockRequest.getRemoteHost()).thenReturn("domain.net");
        when(mockRequest.getServerPort()).thenReturn(80);
        edgeRequestWrapper = new EdgeHttpServletRequestWrapper(mockRequest);
        when(zuulRequestWrapper.getRequest()).thenReturn(edgeRequestWrapper);

        subject = new AuditingService(auditLogRecordCache, routeLocator);
    }

    @Test
    public void testCreateAuditLogRecordFromEvent() {
        // given
        LocalDateTime now = LocalDateTime.of(1999, 1, 2, 3, 4, 5);
        RequestInitiatedEvent requestEvent = RequestInitiatedEvent.builder().httpServletRequest(edgeRequestWrapper).initiatedTime(now).build();
        Route route = mock(Route.class);
        when(route.getId()).thenReturn("validServiceId");
        when(routeLocator.getMatchingRoute(edgeRequestWrapper.getRequestURI())).thenReturn(route);

        // when
        subject.createAuditLogRecordFrom(requestEvent);

        // then
        ArgumentCaptor<AuditLogRecord> argumentCaptor = ArgumentCaptor.forClass(AuditLogRecord.class);
        verify(auditLogRecordCache).cache(argumentCaptor.capture());
        AuditLogRecord auditLogRecord = argumentCaptor.getValue();
        assertThat(auditLogRecord.getRequestUri()).isEqualTo(edgeRequestWrapper.getRequestURI());
        assertThat(auditLogRecord.getServiceId()).isEqualTo(route.getId());
        assertThat(auditLogRecord.getRequestDate()).isEqualTo("1999-01-02T03:04:05");
        assertThat(auditLogRecord.getHost()).isEqualTo("domain.net:80");
    }

    @Test
    public void testGetAuditLogRecord() {
        // given
        String requestId = UUID.randomUUID().toString();
        edgeRequestWrapper.setRequestId(requestId);

        AuditLogRecord expectedRecord = new AuditLogRecord();
        when(auditLogRecordCache.findById(requestId)).thenReturn(expectedRecord);

        // when
        AuditLogRecord auditLogRecord = subject.getAuditLogRecordFor(zuulRequestWrapper);

        // then
        assertThat(auditLogRecord).isSameAs(expectedRecord);
    }
}