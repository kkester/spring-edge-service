package io.pivotal.edge.security;

import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import io.pivotal.edge.events.EventPublisher;
import io.pivotal.edge.keys.ClientKey;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.cloud.netflix.zuul.filters.Route;
import org.springframework.cloud.netflix.zuul.filters.RouteLocator;
import org.springframework.http.HttpHeaders;
import org.springframework.util.Base64Utils;

import javax.servlet.http.HttpServletRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SecurityFilterTest {

    private SecurityFilter subject;

    @Mock
    private RequestContext requestContext;

    @Mock
    private SecurityService securityService;

    @Mock
    private RouteLocator routeLocator;

    @Mock
    private EventPublisher eventPublisher;

    @Before
    public void setUp() {

        RequestContext.testSetCurrentContext(requestContext);

        subject = new SecurityFilter(securityService, routeLocator, eventPublisher);
    }

    @Test
    public void testRunGivenContextWithApiKeyQueryParameter() throws ZuulException {
        // given
        String apiKey = "1234567890";
        String serviceId = "sid";

        HttpServletRequest httpRequest = Mockito.mock(HttpServletRequest.class);
        when(httpRequest.getParameter("apiKey")).thenReturn(apiKey);
        when(requestContext.getRequest()).thenReturn(httpRequest);
        Route route = Mockito.mock(Route.class);
        when(route.getId()).thenReturn(serviceId);
        when(routeLocator.getMatchingRoute(any())).thenReturn(route);
        when(securityService.getClientKeyWithServiceId(any(), eq(serviceId))).thenReturn(new ClientKey());

        // when
        Object results = subject.run();

        // then
        assertThat(results).isNull();
        ArgumentCaptor<ClientSecretCredentials> argumentCaptor = ArgumentCaptor.forClass(ClientSecretCredentials.class);
        verify(securityService).getClientKeyWithServiceId(argumentCaptor.capture(), eq(serviceId));
        ClientSecretCredentials clientSecretCredentials = argumentCaptor.getValue();
        assertThat(clientSecretCredentials.getClientKey()).isEqualTo(apiKey);

        ArgumentCaptor<SecurityVerifiedEvent> securityEventArgumentCaptor = ArgumentCaptor.forClass(SecurityVerifiedEvent.class);
        verify(eventPublisher).publishEvent(securityEventArgumentCaptor.capture());
        SecurityVerifiedEvent securityVerifiedEvent = securityEventArgumentCaptor.getValue();
        assertThat(securityVerifiedEvent.getClientKey()).isEqualTo(apiKey);
    }

    @Test
    public void testRunGivenContextWithBasicCredentials() throws ZuulException {
        // given
        String apiKey = "1234567890";
        String secretKey = "secretKey";
        String serviceId = "sid";

        HttpServletRequest httpRequest = Mockito.mock(HttpServletRequest.class);
        when(httpRequest.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("basic " + this.base64EncodeClientCredentials(apiKey, secretKey));
        when(requestContext.getRequest()).thenReturn(httpRequest);
        Route route = Mockito.mock(Route.class);
        when(route.getId()).thenReturn(serviceId);
        when(routeLocator.getMatchingRoute(any())).thenReturn(route);
        when(securityService.getClientKeyWithServiceId(any(), eq(serviceId))).thenReturn(new ClientKey());

        // when
        Object results = subject.run();

        // then
        assertThat(results).isNull();
        ArgumentCaptor<ClientSecretCredentials> credentialsArgumentCaptor = ArgumentCaptor.forClass(ClientSecretCredentials.class);
        verify(securityService).getClientKeyWithServiceId(credentialsArgumentCaptor.capture(), eq(serviceId));
        ClientSecretCredentials clientSecretCredentials = credentialsArgumentCaptor.getValue();
        assertThat(clientSecretCredentials.getClientKey()).isEqualTo(apiKey);
        assertThat(clientSecretCredentials.getSecretKey()).isEqualTo(secretKey);
        assertThat(clientSecretCredentials.getRealm()).isEqualTo("basic");

        ArgumentCaptor<SecurityVerifiedEvent> securityEventArgumentCaptor = ArgumentCaptor.forClass(SecurityVerifiedEvent.class);
        verify(eventPublisher).publishEvent(securityEventArgumentCaptor.capture());
        SecurityVerifiedEvent securityVerifiedEvent = securityEventArgumentCaptor.getValue();
        assertThat(securityVerifiedEvent.getRequest()).isEqualTo(httpRequest);
        assertThat(securityVerifiedEvent.getClientKey()).isEqualTo(apiKey);
    }

    private String base64EncodeClientCredentials(String clientKey, String secretKey) {
        String credentials = clientKey + ":" + secretKey;
        return new String(Base64Utils.encode(credentials.getBytes()));
    }
}