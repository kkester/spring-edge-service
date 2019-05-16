package io.pivotal.edge.auditing;

import com.netflix.zuul.context.RequestContext;
import io.pivotal.edge.servlet.filters.EdgeHttpServletRequestWrapper;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
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

    @Before
    public void setUp() {

        RequestContext.testSetCurrentContext(requestContext);

        subject = new RequestIdFilter();
    }

    @Test
    public void testRun() {

        // given
        EdgeHttpServletRequestWrapper requestWrapper = new EdgeHttpServletRequestWrapper(httpServletRequest);
        requestWrapper.setRequestId(REQUEST_ID);
        when(requestContext.getRequest()).thenReturn(requestWrapper);

        // when
        subject.run();

        // then
        verify(requestContext).put(REQUEST_ID_HEADER_NAME, REQUEST_ID);
        ArgumentCaptor<String> argumentCaptor = ArgumentCaptor.forClass(String.class);
        verify(requestContext).addZuulRequestHeader(eq(REQUEST_ID_HEADER_NAME), argumentCaptor.capture());
        assertThat(argumentCaptor.getValue()).isEqualTo(REQUEST_ID);
        verify(requestContext).addZuulResponseHeader(eq(REQUEST_ID_HEADER_NAME), argumentCaptor.capture());
        assertThat(argumentCaptor.getValue()).isEqualTo(REQUEST_ID);
    }

    @After
    public void tearDown() {
        RequestContext.testSetCurrentContext(null);
    }
}