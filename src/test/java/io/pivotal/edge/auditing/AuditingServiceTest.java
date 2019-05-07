package io.pivotal.edge.auditing;

import io.pivotal.edge.events.RequestInitiatedEvent;
import com.netflix.zuul.http.HttpServletRequestWrapper;
import io.pivotal.edge.servlet.filters.EdgeHttpServletRequestWrapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.cloud.netflix.zuul.filters.Route;
import org.springframework.cloud.netflix.zuul.filters.RouteLocator;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AuditingServiceTest {

    /** CUT */
    private AuditingService subject;

    @Mock
    private AuditLogRecordRepository auditLogRecordRepository;

    @Mock
    private RouteLocator routeLocator;

    @Mock
    private HttpServletRequestWrapper zuulRequestWrapper;

    private EdgeHttpServletRequestWrapper edgeRequestWrapper;

    @Before
    public void setUp() {

        HttpServletRequest mockRequest = Mockito.mock(HttpServletRequest.class);
        when(mockRequest.getRequestURI()).thenReturn("http://domain.net/resource");
        edgeRequestWrapper = new EdgeHttpServletRequestWrapper(mockRequest);
        when(zuulRequestWrapper.getRequest()).thenReturn(edgeRequestWrapper);

        subject = new AuditingService(auditLogRecordRepository, routeLocator);
    }

    @Test
    public void testCreateAuditLogRecordFromEvent() {
        // given
        LocalDateTime now = LocalDateTime.of(1999, 1, 2, 3, 4, 5);
        RequestInitiatedEvent requestEvent = RequestInitiatedEvent.builder().httpServletRequest(edgeRequestWrapper).initiatedTime(now).build();
        Route route = Mockito.mock(Route.class);
        when(route.getId()).thenReturn("validServiceId");
        when(routeLocator.getMatchingRoute(edgeRequestWrapper.getRequestURI())).thenReturn(route);

        // when
        subject.createAuditLogRecordFrom(requestEvent);
        AuditLogRecord auditLogRecord = subject.getAuditLogRecordFor(zuulRequestWrapper);

        // then
        assertThat(auditLogRecord).isNotNull();
        assertThat(auditLogRecord.getRequestUri()).isEqualTo(edgeRequestWrapper.getRequestURI());
        assertThat(auditLogRecord.getServiceId()).isEqualTo(route.getId());
        assertThat(auditLogRecord.getRequestDate()).isEqualTo("1999-01-02T03:04:05");
    }
}