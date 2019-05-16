package io.pivotal.edge.keys;

import io.pivotal.edge.keys.domain.ClientDetailServiceEntity;
import io.pivotal.edge.keys.domain.ClientDetailServiceEntityRepository;
import io.pivotal.edge.keys.domain.ClientDetailsEntity;
import io.pivotal.edge.keys.domain.ClientDetailsEntityRepository;
import io.pivotal.edge.keys.web.ApplicationType;
import io.pivotal.edge.keys.web.ClientKey;
import io.pivotal.edge.keys.web.ClientService;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class ClientKeyCache {

    private ClientDetailsEntityRepository clientDetailsRepository;

    private ClientDetailServiceEntityRepository clientDetailServiceEntityRepository;

    private Map<String, ClientKey> clientKeyCache = new HashMap<>();

    public ClientKeyCache(ClientDetailsEntityRepository clientDetailsRepository, ClientDetailServiceEntityRepository clientDetailServiceEntityRepository) {
        this.clientDetailsRepository = clientDetailsRepository;
        this.clientDetailServiceEntityRepository = clientDetailServiceEntityRepository;
    }

    public Collection<ClientKey> findAll() {
        List<ClientKey> clientKeys = clientDetailsRepository.findAll().stream()
                .map(this::map)
                .collect(Collectors.toList());
        return clientKeys;
    }

    private ClientKey map(ClientDetailsEntity clientDetailsEntity) {
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

        List<ClientDetailServiceEntity> clientDetailServiceEntities = clientDetailServiceEntityRepository.findAllByKeyClientId(clientDetailsEntity.getClientId());
        List<ClientService> clientServices = clientDetailServiceEntities.stream()
                .map(this::map)
                .collect(Collectors.toList());
        clientKey.setServices(clientServices);

        return clientKey;
    }

    private ClientService map(ClientDetailServiceEntity clientServiceEntity) {
        ClientService clientService = new ClientService();
        clientService.setId(clientServiceEntity.getKey().getServiceId());
        clientService.setPath(clientServiceEntity.getPath());
        return clientService;
    }

    public Optional<ClientKey> findById(String clientId) {

        ClientKey clientKey = clientKeyCache.get(clientId);
        if (Objects.nonNull(clientKey)) {
            return Optional.of(clientKey);
        }

        Optional<ClientDetailsEntity> clientDetailsEntityOptional = clientDetailsRepository.findById(clientId);
        clientKey = this.map(clientDetailsEntityOptional.get());
        clientKeyCache.put(clientKey.getClientId(), clientKey);
        return clientDetailsEntityOptional.isPresent() ? Optional.of(clientKey) : Optional.empty();
    }

    public void save(ClientDetailsEntity clientDetailsEntity, List<ClientDetailServiceEntity> clientDetailServiceEntities) {

        clientDetailsRepository.save(clientDetailsEntity);
        clientDetailServiceEntityRepository.saveAll(clientDetailServiceEntities);

        ClientKey clientKey = this.map(clientDetailsEntity);
        clientKeyCache.put(clientKey.getClientId(), clientKey);
    }

    public void deleteById(String clientId) {
        clientKeyCache.remove(clientId);
        clientDetailsRepository.deleteById(clientId);
        clientDetailServiceEntityRepository.deleteAllByKeyClientId(clientId);
    }
}
