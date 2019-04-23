package io.pivotal.edge.security;

import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
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

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    @Before
    public void setUp() {

        RequestContext.testSetCurrentContext(requestContext);

        subject = new SecurityFilter(securityService, routeLocator);
    }

    @Test
    public void testRunGivenContextWithApiKeyQueryParameter() throws ZuulException {
        // given
        String apiKey = "1234567890";
        String serviceId = "sid";

        Map<String, List<String>> queryParams = new HashMap<>();
        queryParams.put("apiKey", Arrays.asList(apiKey));
        when(requestContext.getRequestQueryParams()).thenReturn(queryParams);
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
    }
}