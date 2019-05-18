package io.pivotal.edge.keys.filters;

import com.netflix.zuul.context.RequestContext;
import io.pivotal.edge.routing.EdgeRequestContext;
import io.pivotal.edge.events.EventPublisher;
import io.pivotal.edge.events.ClientIdentifiedEvent;
import io.pivotal.edge.keys.filters.ClientIdentityErrorFilter;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

import static io.pivotal.edge.EdgeApplicationConstants.EDGE_REQUEST_CONTEXT;
import static io.pivotal.edge.security.SecurityUtil.base64EncodeClientCredentials;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ClientIdentityErrorFilterTest {

    private static final String API_KEY = "1234567890";

    private ClientIdentityErrorFilter subject;

    @Mock
    private ClientIdentityService clientIdentityService;

    @Mock
    private RequestContext requestContext;

    @Mock
    private EventPublisher eventPublisher;

    @Mock
    private HttpServletRequest httpServletRequest;

    private EdgeRequestContext edgeRequestContext;

    @Before
    public void setUp() {

        RequestContext.testSetCurrentContext(requestContext);
        when(requestContext.getRequest()).thenReturn(httpServletRequest);

        edgeRequestContext = new EdgeRequestContext();

        subject = new ClientIdentityErrorFilter(clientIdentityService, eventPublisher);
    }

    @Test
    public void testRun() {
        // given
        edgeRequestContext.setClientId(API_KEY);
        edgeRequestContext.setRequestId(UUID.randomUUID().toString());
        when(requestContext.get(EDGE_REQUEST_CONTEXT)).thenReturn(edgeRequestContext);

        // when
        Object results = subject.run();

        // then
        assertThat(results).isNull();
        ArgumentCaptor<ClientIdentifiedEvent> securityEventArgumentCaptor = ArgumentCaptor.forClass(ClientIdentifiedEvent.class);
        verify(eventPublisher).publishEvent(securityEventArgumentCaptor.capture());
        ClientIdentifiedEvent clientIdentifiedEvent = securityEventArgumentCaptor.getValue();
        assertThat(clientIdentifiedEvent.getClientKey()).isEqualTo(API_KEY);
    }

    @Test
    public void testRunWhenMissingRequestContext() {
        // given
        when(requestContext.get(EDGE_REQUEST_CONTEXT)).thenReturn(null);
        when(clientIdentityService.createEdgeRequestContextFrom(any())).thenReturn(edgeRequestContext);

        // when
        Object results = subject.run();

        // then
        assertThat(results).isNull();
        verify(eventPublisher, never()).publishEvent(any());
    }

    @After
    public void tearDown() {
        RequestContext.testSetCurrentContext(null);
    }

}