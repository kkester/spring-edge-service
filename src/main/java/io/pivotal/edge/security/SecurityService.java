package io.pivotal.edge.security;

import io.pivotal.edge.keys.ClientKey;
import io.pivotal.edge.keys.ClientKeyRepository;
import io.pivotal.edge.keys.ClientService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class SecurityService {

    @Autowired
    private ClientKeyRepository clientKeyRepository;


    public ClientKey getClientKeyWithServiceId(ClientSecretCredentials credentials, String serviceId) {

        Optional<ClientKey> clientKeyOptional = clientKeyRepository.findById(credentials.getClientKey());
        if (!clientKeyOptional.isPresent()) {
            return null;
        }

        ClientKey clientKey = clientKeyOptional.get();
        if (!StringUtils.equals(credentials.getSecretKey(), clientKey.getSecretKey())) {
            return null;
        }

        Optional<ClientService> serviceOptional = this.findClientService(serviceId, clientKey);
        return serviceOptional.isPresent() ? clientKey : null;
    }

    private Optional<ClientService> findClientService(String serviceId, ClientKey clientKey) {
        return clientKey.getServices() == null ?
                Optional.empty() :
                clientKey.getServices().stream().filter(s -> s.getId().equalsIgnoreCase(serviceId)).findFirst();
    }

}
