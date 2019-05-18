package io.pivotal.edge.keys.filters;

import io.pivotal.edge.keys.ClientKeyConverter;
import io.pivotal.edge.keys.domain.*;
import io.pivotal.edge.keys.web.ApplicationType;
import io.pivotal.edge.keys.web.ClientKey;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

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
    private BCryptPasswordEncoder passwordEncoder;

    private String clientId;

    private String secretKey;

    @Before
    public void setUp() {

        clientId = UUID.randomUUID().toString();
        secretKey = UUID.randomUUID().toString();

        subject = new ClientIdentityService(new ClientKeyCache(), new ClientKeyConverter(passwordEncoder), clientDetailsRepository, clientDetailsServiceEntityRepository);
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

}