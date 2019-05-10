package io.pivotal.edge.routing;

import com.netflix.zuul.context.RequestContext;
import io.pivotal.edge.keys.ClientService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.cloud.netflix.zuul.filters.Route;

import java.net.URISyntaxException;
import java.net.URL;

import static io.pivotal.edge.EdgeApplicationConstants.ROUTE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ServiceRouteFilterTest {

    private static final String SERVICE_ID = "sid";

    private ServiceRouteFilter subject;

    @Mock
    private RequestContext requestContext;

    @Mock
    private ClientRoutingService clientRoutingService;

    @Mock
    private Route route;

    private ClientService clientService;

    @Before
    public void setUp() {

        RequestContext.testSetCurrentContext(requestContext);
        when(requestContext.get(ROUTE)).thenReturn(route);

        when(route.getId()).thenReturn(SERVICE_ID);
        clientService = new ClientService();
        clientService.setId(SERVICE_ID);
        when(clientRoutingService.getClientServiceWithServiceId(any(), eq(SERVICE_ID))).thenReturn(clientService);

        subject = new ServiceRouteFilter(clientRoutingService);
    }

    @Test
    public void testRun() throws URISyntaxException {
        // given
        String path = "https://somedomain.net";
        clientService.setPath(path);

        // when
        subject.run();

        // then
        ArgumentCaptor<URL> argumentCaptor = ArgumentCaptor.forClass(URL.class);
        verify(requestContext).setRouteHost(argumentCaptor.capture());
        assertThat(argumentCaptor.getValue().toURI().toString()).isEqualTo(path);
    }

    @After
    public void tearDown() {
        RequestContext.testSetCurrentContext(null);
    }
}