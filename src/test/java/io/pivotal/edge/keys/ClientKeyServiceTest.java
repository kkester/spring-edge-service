package io.pivotal.edge.keys;

import io.pivotal.edge.keys.domain.*;
import io.pivotal.edge.keys.web.ApplicationType;
import io.pivotal.edge.keys.web.ClientKey;
import io.pivotal.edge.keys.web.ClientService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ClientKeyServiceTest {

    /** CUT */
    private ClientKeyService subject;

    @Mock
    private ClientDetailsEntityRepository clientDetailsRepository;

    @Mock
    private ClientDetailServiceEntityRepository clientDetailServiceEntityRepository;

    private String clientId;

    private String secretKey;

    @Before
    public void setUp() {

        clientId = UUID.randomUUID().toString();
        secretKey = UUID.randomUUID().toString();

        subject = new ClientKeyService(new ClientKeyCache(clientDetailsRepository, clientDetailServiceEntityRepository));
    }

    @Test
    public void testFindConfidentialClientKeyById() {
        // given
        ClientDetailsEntity clientDetailsEntity = new ClientDetailsEntity();
        clientDetailsEntity.setClientId(clientId);
        clientDetailsEntity.setClientSecret(secretKey);
        clientDetailsEntity.setAuthorizedGrantTypes("client_credentials");
        when(clientDetailsRepository.findById(clientId)).thenReturn(Optional.of(clientDetailsEntity));

        ClientDetailServiceEntity clientServiceEntity = new ClientDetailServiceEntity();
        ClientDetailServiceKey clientServiceKey = new ClientDetailServiceKey();
        clientServiceKey.setServiceId("test");
        clientServiceEntity.setKey(clientServiceKey);
        clientServiceEntity.setPath("http://somedomain.net");
        when(clientDetailServiceEntityRepository.findAllByKeyClientId(clientId)).thenReturn(Arrays.asList(clientServiceEntity));

        // when
        ClientKey clientKey = subject.findById(clientId);

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
    public void testCreateConfidentialClientKey() {
        // given
        ClientKey clientKey = new ClientKey();
        clientKey.setApplicationType(ApplicationType.CONFIDENTIAL);
        ClientService clientService = new ClientService();
        clientService.setId("test");
        clientService.setPath("http://somedomain.net");
        clientKey.setServices(Arrays.asList(clientService));

        // when
        ClientKey clientKeyResults = subject.createClientKey(clientKey);

        // then
        assertThat(clientKeyResults).isNotNull();
        ArgumentCaptor<ClientDetailsEntity> clientDetailsEntityArgumentCaptor = ArgumentCaptor.forClass(ClientDetailsEntity.class);
        verify(clientDetailsRepository).save(clientDetailsEntityArgumentCaptor.capture());
        ClientDetailsEntity clientDetailsEntity = clientDetailsEntityArgumentCaptor.getValue();
        assertThat(clientDetailsEntity.getClientId()).isNotBlank();
        assertThat(clientDetailsEntity.getAuthorizedGrantTypes()).isEqualTo("authorization_code,password,refresh_token,client_credentials");

        ArgumentCaptor<List<ClientDetailServiceEntity>> clientServiceEntityArgumentCaptor = ArgumentCaptor.forClass(List.class);
        verify(clientDetailServiceEntityRepository).saveAll(clientServiceEntityArgumentCaptor.capture());
        List<ClientDetailServiceEntity> clientServiceEntities = clientServiceEntityArgumentCaptor.getValue();
        assertThat(clientServiceEntities).hasSize(1);
        assertThat(clientServiceEntities.get(0).getKey().getServiceId()).isEqualTo(clientService.getId());
        assertThat(clientServiceEntities.get(0).getKey().getClientId()).isEqualTo(clientDetailsEntity.getClientId());
        assertThat(clientServiceEntities.get(0).getPath()).isEqualTo(clientService.getPath());
    }

    @Test
    public void testCreatePublicClientKey() {
        // given
        ClientKey clientKey = new ClientKey();
        clientKey.setApplicationType(ApplicationType.PUBLIC);
        ClientService clientService = new ClientService();
        clientService.setId("test");
        clientService.setPath("http://somedomain.net");
        clientKey.setServices(Arrays.asList(clientService));

        // when
        ClientKey clientKeyResults = subject.createClientKey(clientKey);

        // then
        assertThat(clientKeyResults).isNotNull();
        ArgumentCaptor<ClientDetailsEntity> clientDetailsEntityArgumentCaptor = ArgumentCaptor.forClass(ClientDetailsEntity.class);
        verify(clientDetailsRepository).save(clientDetailsEntityArgumentCaptor.capture());
        ClientDetailsEntity clientDetailsEntity = clientDetailsEntityArgumentCaptor.getValue();
        assertThat(clientDetailsEntity.getClientId()).isNotBlank();
        assertThat(clientDetailsEntity.getAuthorizedGrantTypes()).isEqualTo("implicit,password,authorization_code");
    }
}