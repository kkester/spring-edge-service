package io.pivotal.edge.auditing;

import io.pivotal.edge.events.RequestInitiatedEvent;
import io.pivotal.edge.servlet.filters.EdgeHttpServletRequestWrapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.cloud.netflix.zuul.filters.Route;
import org.springframework.cloud.netflix.zuul.filters.RouteLocator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
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
    private EdgeHttpServletRequestWrapper request;

    @Before
    public void setUp() {

        when(request.getRequestURI()).thenReturn("http://domain.net/resource");

        subject = new AuditingService(auditLogRecordRepository, routeLocator);
    }

    @Test
    public void testCreateAuditLogRecordFromEvent() {
        // given
        RequestInitiatedEvent requestEvent = RequestInitiatedEvent.builder().httpServletRequest(request).build();
        Route route = Mockito.mock(Route.class);
        when(route.getId()).thenReturn("validServiceId");
        when(routeLocator.getMatchingRoute(request.getRequestURI())).thenReturn(route);

        // when
        subject.createAuditLogRecordFrom(requestEvent);

        // then
        ArgumentCaptor<AuditLogRecord> auditLogRecordArgumentCaptor = ArgumentCaptor.forClass(AuditLogRecord.class);
        verify(auditLogRecordRepository).save(auditLogRecordArgumentCaptor.capture());
        AuditLogRecord auditLogRecord = auditLogRecordArgumentCaptor.getValue();
        assertThat(auditLogRecord.getRequestUri()).isEqualTo(request.getRequestURI());
        assertThat(auditLogRecord.getServiceId()).isEqualTo(route.getId());
    }
}