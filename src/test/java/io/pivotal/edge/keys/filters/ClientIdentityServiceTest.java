package io.pivotal.edge.keys.filters;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.netflix.zuul.context.RequestContext;
import io.pivotal.edge.keys.ClientKeyConverter;
import io.pivotal.edge.keys.domain.*;
import io.pivotal.edge.keys.web.ApplicationType;
import io.pivotal.edge.keys.web.ClientKey;
import io.pivotal.edge.routing.EdgeRequestContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpHeaders;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ClientIdentityServiceTest {

    /**
     * CUT
     */
    private ClientIdentityService subject;

    @Mock
    private ClientDetailsEntityRepository clientDetailsRepository;

    @Mock
    private ClientDetailsServiceEntityRepository clientDetailsServiceEntityRepository;

    @Mock
    private RequestContext requestContext;

    private String clientId;

    private String secretKey;

    @Before
    public void setUp() {

        RequestContext.testSetCurrentContext(requestContext);

        clientId = UUID.randomUUID().toString();
        secretKey = UUID.randomUUID().toString();

        subject = new ClientIdentityService(new ClientKeyCache(), new ClientKeyConverter(null), clientDetailsRepository, clientDetailsServiceEntityRepository, new ObjectMapper());
    }

    @Test
    public void testFindCachedConfidentialClientKeyById() {
        // given
        ClientDetailsEntity clientDetailsEntity = new ClientDetailsEntity();
        clientDetailsEntity.setClientId(clientId);
        clientDetailsEntity.setClientSecret(secretKey);
        clientDetailsEntity.setAuthorizedGrantTypes("client_credentials");
        when(clientDetailsRepository.findById(clientId)).thenReturn(Optional.of(clientDetailsEntity));

        ClientDetailsServiceEntity clientServiceEntity = new ClientDetailsServiceEntity();
        ClientDetailsServiceKey clientServiceKey = new ClientDetailsServiceKey();
        clientServiceKey.setServiceId("test");
        clientServiceEntity.setKey(clientServiceKey);
        clientServiceEntity.setPath("http://somedomain.net");
        when(clientDetailsServiceEntityRepository.findAllByKeyClientId(clientId)).thenReturn(Arrays.asList(clientServiceEntity));

        // when
        ClientKey clientKey = subject.findCachedClientKeyById(clientId);

        // then
        assertThat(clientKey).isNotNull();
        assertThat(clientKey.getClientId()).isEqualTo(clientId);
        assertThat(clientKey.getApplicationType()).isEqualTo(ApplicationType.CONFIDENTIAL);
        assertThat(clientKey.getSecretKey()).isEqualTo(secretKey);
        assertThat(clientKey.getServices()).hasSize(1);
        assertThat(clientKey.getServices().get(0).getId()).isEqualTo(clientServiceKey.getServiceId());
        assertThat(clientKey.getServices().get(0).getPath()).isEqualTo(clientServiceEntity.getPath());
    }

    @Test
    public void testCreateEdgeRequestContextFromRequestContainingBearerToken() {
        // given
        String bearerToken = "bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJjbGllbnRfaWQiOiI4NzUxYzhjN2U1MGM0NTIzODJjOTY0NGYyYzY0ZWMxYSJ9.zXkPsEPZN3X8iwEtTMf5np17UH4iq2yeWJbadmzlb78";
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn(bearerToken);
        when(requestContext.getRequest()).thenReturn(request);

        // when
        EdgeRequestContext edgeRequestContext = subject.createEdgeRequestContextFrom(requestContext);

        // then
        assertThat(edgeRequestContext).isNotNull();
        assertThat(edgeRequestContext.getAuthorizationType()).isEqualTo("bearer");
        assertThat(edgeRequestContext.getClientId()).isEqualTo("8751c8c7e50c452382c9644f2c64ec1a");
    }

    @After
    public void tearDown() {
        RequestContext.testSetCurrentContext(null);
    }
}