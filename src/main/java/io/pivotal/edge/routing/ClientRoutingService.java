package io.pivotal.edge.routing;

import io.pivotal.edge.keys.ClientKey;
import io.pivotal.edge.keys.ClientKeyRepository;
import io.pivotal.edge.keys.ClientService;
import io.pivotal.edge.security.ClientSecretCredentials;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ClientRoutingService {

    @Autowired
    private ClientKeyRepository clientKeyRepository;


    private Optional<ClientService> findClientService(String serviceId, ClientKey clientKey) {
        return clientKey.getServices() == null ?
                Optional.empty() :
                clientKey.getServices().stream().filter(s -> s.getId().equalsIgnoreCase(serviceId)).findFirst();
    }

    public ClientService getClientServiceWithServiceId(ClientSecretCredentials credentials, String serviceId) {

        Optional<ClientKey> clientKeyOptional = clientKeyRepository.findById(credentials.getClientKey());
        if (!clientKeyOptional.isPresent()) {
            return null;
        }

        Optional<ClientService> serviceOptional = this.findClientService(serviceId, clientKeyOptional.get());
        return serviceOptional.orElse(null);
    }
}
