package io.pivotal.edge.auditing;

import com.netflix.zuul.context.RequestContext;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.servlet.http.HttpServletRequest;
import java.util.UUID;

import static io.pivotal.edge.EdgeApplicationConstants.REQUEST_ID_HEADER_NAME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@RunWith(MockitoJUnitRunner.class)
public class RequestIdFilterTest {

    private static final String REQUEST_ID = UUID.randomUUID().toString();

    private RequestIdFilter subject;

    @Mock
    private RequestContext requestContext;

    @Mock
    private AuditingService auditingService;

    @Mock
    private HttpServletRequest httpServletRequest;

    private AuditLogRecord auditLogRecord;

    @Before
    public void setUp() {

        RequestContext.testSetCurrentContext(requestContext);
        when(requestContext.getRequest()).thenReturn(httpServletRequest);

        auditLogRecord = new AuditLogRecord();
        auditLogRecord.setId(REQUEST_ID);

        subject = new RequestIdFilter(auditingService);
    }

    @Test
    public void testRun() {
        // given
        when(auditingService.getAuditLogRecordFor(httpServletRequest)).thenReturn(auditLogRecord);

        // when
        subject.run();

        // then
        ArgumentCaptor<String> argumentCaptor = ArgumentCaptor.forClass(String.class);
        verify(requestContext).addZuulRequestHeader(eq(REQUEST_ID_HEADER_NAME), argumentCaptor.capture());
        assertThat(argumentCaptor.getValue()).isEqualTo(REQUEST_ID);
        verify(requestContext).addZuulResponseHeader(eq(REQUEST_ID_HEADER_NAME), argumentCaptor.capture());
        assertThat(argumentCaptor.getValue()).isEqualTo(REQUEST_ID);
    }

}