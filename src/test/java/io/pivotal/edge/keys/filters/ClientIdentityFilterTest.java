package io.pivotal.edge.keys.filters;

import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import io.pivotal.edge.EdgeRequestContext;
import io.pivotal.edge.events.ClientIdentifiedEvent;
import io.pivotal.edge.events.EventPublisher;
import io.pivotal.edge.keys.ClientKeyService;
import io.pivotal.edge.keys.web.ApplicationType;
import io.pivotal.edge.keys.web.ClientKey;
import io.pivotal.edge.servlet.filters.EdgeHttpServletRequestWrapper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpHeaders;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

import static io.pivotal.edge.EdgeApplicationConstants.EDGE_REQUEST_CONTEXT;
import static io.pivotal.edge.security.SecurityUtil.base64EncodeClientCredentials;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ClientIdentityFilterTest {

    /**
     * CUT
     */
    private ClientIdentityFilter subject;

    @Mock
    private RequestContext requestContext;

    @Mock
    private ClientKeyService clientKeyService;

    @Mock
    private EventPublisher eventPublisher;

    @Before
    public void setUp() {

        RequestContext.testSetCurrentContext(requestContext);

        subject = new ClientIdentityFilter(new ClientIdentityService(), clientKeyService, eventPublisher);
    }

    @Test
    public void testRunGivenContextWithApiKeyQueryParameter() throws ZuulException {
        // given
        String apiKey = "1234567890";

        Map<String, List<String>> requestQueryParams = new HashMap<>();
        requestQueryParams.put("apiKey", Arrays.asList(apiKey));
        when(requestContext.getRequestQueryParams()).thenReturn(requestQueryParams);

        ClientKey clientKey = new ClientKey();
        clientKey.setApplicationType(ApplicationType.PUBLIC);
        when(clientKeyService.findById(apiKey)).thenReturn(clientKey);

        // when
        Object results = subject.run();

        // then
        assertThat(results).isNull();
        ArgumentCaptor<EdgeRequestContext> edgeRequestContextArgumentCaptor = ArgumentCaptor.forClass(EdgeRequestContext.class);
        verify(requestContext).set(eq(EDGE_REQUEST_CONTEXT), edgeRequestContextArgumentCaptor.capture());
        EdgeRequestContext edgeRequestContext = edgeRequestContextArgumentCaptor.getValue();
        assertThat(edgeRequestContext.getClientId()).isEqualTo(apiKey);

        ArgumentCaptor<ClientIdentifiedEvent> eventArgumentCaptor = ArgumentCaptor.forClass(ClientIdentifiedEvent.class);
        verify(eventPublisher).publishEvent(eventArgumentCaptor.capture());
        ClientIdentifiedEvent clientIdentifiedEvent = eventArgumentCaptor.getValue();
        assertThat(clientIdentifiedEvent.getClientKey()).isEqualTo(apiKey);

        ArgumentCaptor<Map<String, List<String>>> requestQueryParamCaptor = ArgumentCaptor.forClass(Map.class);
        verify(requestContext).setRequestQueryParams(requestQueryParamCaptor.capture());
        assertThat(requestQueryParamCaptor.getValue()).doesNotContainKeys("apiKey");
    }

    @Test
    public void testRunGivenContextWithBasicCredentials() throws ZuulException {
        // given
        String apiKey = "1234567890";
        String secretKey = "secretKey";
        String clientSecretKey = "clientSecretKey";

        HttpServletRequest httpRequest = Mockito.mock(HttpServletRequest.class);
        when(httpRequest.getHeaderNames()).thenReturn(Collections.enumeration(Arrays.asList(HttpHeaders.AUTHORIZATION)));
        String basicHeaderValue = "basic " + base64EncodeClientCredentials(apiKey, secretKey);
        when(httpRequest.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn(basicHeaderValue);
        when(httpRequest.getHeaders(HttpHeaders.AUTHORIZATION)).thenReturn(Collections.enumeration(Arrays.asList(basicHeaderValue)));
        EdgeHttpServletRequestWrapper edgeRequestWrapper = new EdgeHttpServletRequestWrapper(httpRequest);
        when(requestContext.getRequest()).thenReturn(edgeRequestWrapper);

        ClientKey clientKey = new ClientKey();
        clientKey.setClientId(apiKey);
        clientKey.setSecretKey(clientSecretKey);
        clientKey.setApplicationType(ApplicationType.CONFIDENTIAL);
        when(clientKeyService.findById(apiKey)).thenReturn(clientKey);

        // when
        Object results = subject.run();

        // then
        assertThat(results).isNull();
        ArgumentCaptor<EdgeRequestContext> requestContextArgumentCaptor = ArgumentCaptor.forClass(EdgeRequestContext.class);
        verify(requestContext).set(eq(EDGE_REQUEST_CONTEXT), requestContextArgumentCaptor.capture());
        EdgeRequestContext edgeRequestContext = requestContextArgumentCaptor.getValue();
        assertThat(edgeRequestContext.getClientId()).isEqualTo(apiKey);
        assertThat(edgeRequestContext.getRequestSecretKey()).isEqualTo(secretKey);

        ArgumentCaptor<ClientIdentifiedEvent> eventArgumentCaptor = ArgumentCaptor.forClass(ClientIdentifiedEvent.class);
        verify(eventPublisher).publishEvent(eventArgumentCaptor.capture());
        ClientIdentifiedEvent clientIdentifiedEvent = eventArgumentCaptor.getValue();
        assertThat(clientIdentifiedEvent.getRequest()).isEqualTo(edgeRequestWrapper);
        assertThat(clientIdentifiedEvent.getClientKey()).isEqualTo(apiKey);

        assertThat(edgeRequestWrapper.getHeader(HttpHeaders.AUTHORIZATION)).isNull();
    }

    @After
    public void tearDown() {
        RequestContext.testSetCurrentContext(null);
    }

}