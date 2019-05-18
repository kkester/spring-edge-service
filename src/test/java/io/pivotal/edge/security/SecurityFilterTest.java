package io.pivotal.edge.security;

import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import com.netflix.zuul.monitoring.CounterFactory;
import io.pivotal.edge.routing.EdgeRequestContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.cloud.netflix.zuul.filters.Route;
import org.springframework.cloud.netflix.zuul.filters.RouteLocator;
import org.springframework.http.HttpStatus;

import static io.pivotal.edge.EdgeApplicationConstants.EDGE_REQUEST_CONTEXT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SecurityFilterTest {

    private SecurityFilter subject;

    @Mock
    private RequestContext requestContext;

    @Mock
    private CounterFactory counterFactory;

    @Mock
    private SecurityService securityService;

    @Mock
    private RouteLocator routeLocator;

    @Before
    public void setUp() {

        RequestContext.testSetCurrentContext(requestContext);
        CounterFactory.initialize(counterFactory);

        subject = new SecurityFilter(securityService, routeLocator);
    }

    @Test
    public void testRun() throws ZuulException {
        // given
        String apiKey = "1234567890";
        String serviceId = "sid";
        EdgeRequestContext edgeRequestContext = new EdgeRequestContext();
        edgeRequestContext.setClientId(apiKey);
        edgeRequestContext.setServiceId(serviceId);
        when(requestContext.get(EDGE_REQUEST_CONTEXT)).thenReturn(edgeRequestContext);

        Route route = Mockito.mock(Route.class);
        when(route.getId()).thenReturn(serviceId);
        when(routeLocator.getMatchingRoute(any())).thenReturn(route);

        when(securityService.validate(edgeRequestContext)).thenReturn(true);

        // when
        Object results = subject.run();

        // then
        assertThat(results).isNull();
        verify(securityService).validate(edgeRequestContext);
    }

    @Test
    public void testRunGivenInvalidRequestContext() throws ZuulException {
        // given
        String apiKey = "1234567890";
        String serviceId = "sid";
        EdgeRequestContext edgeRequestContext = new EdgeRequestContext();
        edgeRequestContext.setClientId(apiKey);
        edgeRequestContext.setServiceId(serviceId);
        when(requestContext.get(EDGE_REQUEST_CONTEXT)).thenReturn(edgeRequestContext);

        Route route = Mockito.mock(Route.class);
        when(route.getId()).thenReturn(serviceId);
        when(routeLocator.getMatchingRoute(any())).thenReturn(route);

        when(securityService.validate(edgeRequestContext)).thenReturn(false);

        // when
        try {
            subject.run();
            fail();
        } catch (ZuulException e) {
            assertThat(e.nStatusCode).isEqualTo(HttpStatus.FORBIDDEN.value());
        }
    }

    @Test
    public void testRunWhenMissingRequestContext() {
        // given
        when(requestContext.get(EDGE_REQUEST_CONTEXT)).thenReturn(null);

        Route route = Mockito.mock(Route.class);
        when(routeLocator.getMatchingRoute(any())).thenReturn(route);

        // when
        try {
            subject.run();
            fail();
        } catch (ZuulException e) {
            assertThat(e.nStatusCode).isEqualTo(HttpStatus.FORBIDDEN.value());
        }
    }

    @After
    public void tearDown() {
        RequestContext.testSetCurrentContext(null);
        CounterFactory.initialize(null);
    }

}