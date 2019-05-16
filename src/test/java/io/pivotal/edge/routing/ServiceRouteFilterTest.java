package io.pivotal.edge.routing;

import com.netflix.zuul.context.RequestContext;
import io.pivotal.edge.EdgeRequestContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import static io.pivotal.edge.EdgeApplicationConstants.EDGE_REQUEST_CONTEXT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ServiceRouteFilterTest {

    private static final String SERVICE_ID = "sid";

    private ServiceRouteFilter subject;

    @Mock
    private RequestContext requestContext;

    private Map<String, String> allowedServices = new HashMap<>();

    @Before
    public void setUp() {

        RequestContext.testSetCurrentContext(requestContext);

        subject = new ServiceRouteFilter();
    }

    @Test
    public void testRun() throws URISyntaxException {
        // given
        String path = "https://somedomain.net";
        allowedServices.put(SERVICE_ID, path);
        EdgeRequestContext edgeRequestContext = new EdgeRequestContext();
        edgeRequestContext.setServiceId(SERVICE_ID);
        edgeRequestContext.setAllowedServices(allowedServices);
        when(requestContext.get(EDGE_REQUEST_CONTEXT)).thenReturn(edgeRequestContext);

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