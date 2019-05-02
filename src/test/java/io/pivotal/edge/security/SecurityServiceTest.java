package io.pivotal.edge.security;

import io.pivotal.edge.keys.ApplicationType;
import io.pivotal.edge.keys.ClientKey;
import io.pivotal.edge.keys.ClientKeyRepository;
import io.pivotal.edge.keys.ClientService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SecurityServiceTest {

    private SecurityService subject;

    @Mock
    private ClientKeyRepository clientKeyRepository;

    @Before
    public void setUp() {
        subject = new SecurityService(clientKeyRepository);
    }

    @Test
    public void testGetClientKeyWithServiceId() {
        // given
        ClientSecretCredentials clientCredentials = Mockito.mock(ClientSecretCredentials.class);
        when(clientCredentials.getClientKey()).thenReturn("ckey");
        ClientKey clientKey = new ClientKey();
        clientKey.setApplicationType(ApplicationType.PUBLIC);
        ClientService service = new ClientService();
        service.setId("sid");
        clientKey.setServices(Arrays.asList(service));
        when(clientKeyRepository.findById("ckey")).thenReturn(Optional.of(clientKey));

        // when
        ClientKey result = subject.getClientKeyWithServiceId(clientCredentials, "sid");

        // then
        assertThat(result).isSameAs(clientKey);
    }
}