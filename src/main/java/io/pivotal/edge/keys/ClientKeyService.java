package io.pivotal.edge.keys;

import io.pivotal.edge.keys.domain.ClientDetailServiceEntity;
import io.pivotal.edge.keys.domain.ClientDetailServiceKey;
import io.pivotal.edge.keys.domain.ClientDetailsEntity;
import io.pivotal.edge.keys.web.ApplicationType;
import io.pivotal.edge.keys.web.ClientKey;
import io.pivotal.edge.keys.web.ClientService;
import io.pivotal.edge.keys.web.ResourceNotFoundException;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ClientKeyService {

    private ClientKeyCache clientKeyCache;

    public ClientKeyService(ClientKeyCache clientKeyCache) {
        this.clientKeyCache = clientKeyCache;
    }

    public Collection<ClientKey> findAll() {
        return clientKeyCache.findAll();
    }

    public ClientKey createClientKey(ClientKey clientKey) {

        String clientId = UUID.randomUUID().toString().replace("-", "");
        String secretKey = UUID.randomUUID().toString().replace("-", "");

        ClientDetailsEntity clientDetailsEntity = new ClientDetailsEntity();
        clientDetailsEntity.setClientId(clientId);
        clientDetailsEntity.setClientSecret(secretKey);
        if (ApplicationType.CONFIDENTIAL.equals(clientKey.getApplicationType())) {
            clientDetailsEntity.setAuthorizedGrantTypes("authorization_code,password,refresh_token,client_credentials");
        } else if (ApplicationType.NATIVE.equals(clientKey.getApplicationType())) {
            clientDetailsEntity.setAuthorizedGrantTypes("implicit,password,device_code");
        } else {
            clientDetailsEntity.setAuthorizedGrantTypes("implicit,password,authorization_code");
        }

        List<ClientDetailServiceEntity> clientDetailServiceEntities = new ArrayList<>();
        clientKey.getServices().forEach( s-> {
            ClientDetailServiceKey key = new ClientDetailServiceKey();
            key.setServiceId(s.getId());
            key.setClientId(clientId);

            ClientDetailServiceEntity clientServiceEntity = new ClientDetailServiceEntity();
            clientServiceEntity.setKey(key);
            clientServiceEntity.setPath(s.getPath());
            clientDetailServiceEntities.add(clientServiceEntity);
        });

        clientKeyCache.save(clientDetailsEntity, clientDetailServiceEntities);
        return clientKeyCache.findById(clientId).get();
    }

    public ClientKey findById(String id) {
        Optional<ClientKey> clientKeyOptional = clientKeyCache.findById(id);
        return clientKeyOptional.orElseThrow(() -> new ResourceNotFoundException("Client Key resource could not be found with for an id of %s", id) );
    }

    public void deleteById(String id) {
        Optional<ClientKey> clientKeyOptional = clientKeyCache.findById(id);
        if (!clientKeyOptional.isPresent()) {
            throw new ResourceNotFoundException("Client Key resource could not be found with for an id of %s", id);
        }
        clientKeyCache.deleteById(id);
    }

}
