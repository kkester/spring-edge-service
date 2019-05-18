package io.pivotal.edge.keys;

import io.pivotal.edge.keys.domain.ClientDetailsServiceEntity;
import io.pivotal.edge.keys.domain.ClientDetailsServiceKey;
import io.pivotal.edge.keys.domain.ClientDetailsEntity;
import io.pivotal.edge.keys.web.ApplicationType;
import io.pivotal.edge.keys.web.ClientKey;
import io.pivotal.edge.keys.web.ClientService;
import org.apache.commons.lang.StringUtils;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ClientKeyConverter {

    private BCryptPasswordEncoder passwordEncoder;

    public ClientKeyConverter(BCryptPasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    public ClientDetailsEntity convertClientKey(ClientKey clientKey) {

        String clientId = clientKey.getClientId();
        ClientDetailsEntity clientDetailsEntity = new ClientDetailsEntity();
        clientDetailsEntity.setClientId(clientId);
        clientDetailsEntity.setClientSecret(passwordEncoder.encode(clientKey.getSecretKey()));
        if (ApplicationType.CONFIDENTIAL.equals(clientKey.getApplicationType())) {
            clientDetailsEntity.setAuthorizedGrantTypes("authorization_code,password,refresh_token,client_credentials");
        } else if (ApplicationType.NATIVE.equals(clientKey.getApplicationType())) {
            clientDetailsEntity.setAuthorizedGrantTypes("implicit,password,device_code");
        } else {
            clientDetailsEntity.setAuthorizedGrantTypes("implicit,password,authorization_code");
        }
        clientDetailsEntity.setScope("read,write");
        clientDetailsEntity.setAccessTokenValidity(clientKey.getAccessTokenValidity());
        clientDetailsEntity.setRefreshTokenValidity(clientKey.getRefreshTokenValidity());

        return clientDetailsEntity;
    }

    public List<ClientDetailsServiceEntity> convertClientService(String clientId, List<ClientService> clientServices) {
        List<ClientDetailsServiceEntity> clientDetailServiceEntities = new ArrayList<>();
        clientServices.forEach(s -> {
            ClientDetailsServiceKey key = new ClientDetailsServiceKey();
            key.setServiceId(s.getId());
            key.setClientId(clientId);

            ClientDetailsServiceEntity clientServiceEntity = new ClientDetailsServiceEntity();
            clientServiceEntity.setKey(key);
            clientServiceEntity.setPath(s.getPath());
            clientDetailServiceEntities.add(clientServiceEntity);
        });
        return clientDetailServiceEntities;
    }

    public ClientKey convertClientDetailsEntity(ClientDetailsEntity clientDetailsEntity, List<ClientDetailsServiceEntity> clientDetailServiceEntities) {
        ClientKey clientKey = new ClientKey();
        clientKey.setClientId(clientDetailsEntity.getClientId());
        clientKey.setSecretKey(clientDetailsEntity.getClientSecret());
        if (StringUtils.containsIgnoreCase(clientDetailsEntity.getAuthorizedGrantTypes(), "client_credentials")) {
            clientKey.setApplicationType(ApplicationType.CONFIDENTIAL);
        } else if (StringUtils.containsIgnoreCase(clientDetailsEntity.getAuthorizedGrantTypes(), "device_code")) {
            clientKey.setApplicationType(ApplicationType.NATIVE);
        } else {
            clientKey.setApplicationType(ApplicationType.PUBLIC);
        }

        List<ClientService> clientServices = clientDetailServiceEntities.stream()
                .map(this::convertClientDetailsServiceEntity)
                .collect(Collectors.toList());
        clientKey.setServices(clientServices);

        return clientKey;
    }

    private ClientService convertClientDetailsServiceEntity(ClientDetailsServiceEntity clientServiceEntity) {
        ClientService clientService = new ClientService();
        clientService.setId(clientServiceEntity.getKey().getServiceId());
        clientService.setPath(clientServiceEntity.getPath());
        return clientService;
    }

}
