package io.pivotal.edge.security;

import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import io.pivotal.edge.events.EventPublisher;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpHeaders;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.pivotal.edge.security.SecurityUtil.base64EncodeClientCredentials;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class SecurityErrorFilterTest {

    private static final String API_KEY = "1234567890";

    private SecurityErrorFilter subject;

    @Mock
    private RequestContext requestContext;

    @Mock
    private EventPublisher eventPublisher;

    @Before
    public void setUp() {

        RequestContext.testSetCurrentContext(requestContext);

        subject = new SecurityErrorFilter(eventPublisher);
    }

    @Test
    public void testRunGivenContextWithApiKeyQueryParameter() {
        // given
        Map<String, List<String>> requestQueryParams = new HashMap<>();
        requestQueryParams.put("apiKey", Arrays.asList(API_KEY));
        when(requestContext.getRequestQueryParams()).thenReturn(requestQueryParams);

        // when
        Object results = subject.run();

        // then
        assertThat(results).isNull();
        ArgumentCaptor<SecurityVerifiedEvent> securityEventArgumentCaptor = ArgumentCaptor.forClass(SecurityVerifiedEvent.class);
        verify(eventPublisher).publishEvent(securityEventArgumentCaptor.capture());
        SecurityVerifiedEvent securityVerifiedEvent = securityEventArgumentCaptor.getValue();
        assertThat(securityVerifiedEvent.getClientKey()).isEqualTo(API_KEY);
    }

    @Test
    public void testRunGivenContextWithBasicCredentials() {
        // given
        String secretKey = "secretKey";

        HttpServletRequest httpRequest = Mockito.mock(HttpServletRequest.class);
        when(httpRequest.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("basic " + base64EncodeClientCredentials(API_KEY, secretKey));
        when(requestContext.getRequest()).thenReturn(httpRequest);

        // when
        Object results = subject.run();

        // then
        assertThat(results).isNull();
        ArgumentCaptor<SecurityVerifiedEvent> securityEventArgumentCaptor = ArgumentCaptor.forClass(SecurityVerifiedEvent.class);
        verify(eventPublisher).publishEvent(securityEventArgumentCaptor.capture());
        SecurityVerifiedEvent securityVerifiedEvent = securityEventArgumentCaptor.getValue();
        assertThat(securityVerifiedEvent.getRequest()).isSameAs(httpRequest);
        assertThat(securityVerifiedEvent.getClientKey()).isEqualTo(API_KEY);
    }

    @Test
    public void testRunGivenContextWithMissingCredentials() {
        // given
        Map<String, List<String>> requestQueryParams = new HashMap<>();
        when(requestContext.getRequestQueryParams()).thenReturn(requestQueryParams);

        // when
        Object results = subject.run();

        // then
        assertThat(results).isNull();
        verify(eventPublisher, never()).publishEvent(any());
    }

}