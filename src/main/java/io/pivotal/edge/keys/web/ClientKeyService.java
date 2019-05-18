package io.pivotal.edge.keys.web;

import io.pivotal.edge.keys.ClientKeyConverter;
import io.pivotal.edge.keys.domain.ClientDetailsEntity;
import io.pivotal.edge.keys.domain.ClientDetailsEntityRepository;
import io.pivotal.edge.keys.domain.ClientDetailsServiceEntity;
import io.pivotal.edge.keys.domain.ClientDetailsServiceEntityRepository;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.*;

@Service
public class ClientKeyService {

    static final String CLIENT_SECRET_MASK = "********";

    private ClientKeyConverter clientKeyConverter;

    private ClientDetailsEntityRepository clientDetailsRepository;

    private ClientDetailsServiceEntityRepository clientDetailsServiceEntityRepository;

    public ClientKeyService(ClientKeyConverter clientKeyConverter, ClientDetailsEntityRepository clientDetailsRepository, ClientDetailsServiceEntityRepository clientDetailsServiceEntityRepository) {
        this.clientKeyConverter = clientKeyConverter;
        this.clientDetailsRepository = clientDetailsRepository;
        this.clientDetailsServiceEntityRepository = clientDetailsServiceEntityRepository;
    }

    public Collection<ClientKey> findAll() {

        List<ClientKey> clientKeys = new ArrayList<>();
        clientDetailsRepository.findAll().stream().forEach(c -> {
            List<ClientDetailsServiceEntity> serviceEntities = clientDetailsServiceEntityRepository.findAllByKeyClientId(c.getClientId());
            ClientKey clientKey = clientKeyConverter.convertClientDetailsEntity(c, serviceEntities);
            clientKey.setSecretKey(CLIENT_SECRET_MASK);
            clientKeys.add(clientKey);
        });
        return clientKeys;
    }

    public ClientKey createClientKey(ClientKey clientKey) {

        clientKey.setClientId(UUID.randomUUID().toString().replace("-", ""));
        clientKey.setSecretKey(UUID.randomUUID().toString().replace("-", ""));

        ClientDetailsEntity clientDetailsEntity = clientKeyConverter.convertClientKey(clientKey);
        clientDetailsRepository.save(clientDetailsEntity);
        List<ClientDetailsServiceEntity> clientDetailServiceEntities = clientKeyConverter.convertClientService(clientKey.getClientId(), clientKey.getServices());
        clientDetailsServiceEntityRepository.saveAll(clientDetailServiceEntities);

        return clientKey;
    }

    public ClientKey findById(String id) {
        ClientKey clientKey = this.getRawClientKeyById(id);
        if (Objects.isNull(clientKey)) {
            throw new ResourceNotFoundException("Client Key resource could not be found with for an id of %s", id);
        }
        clientKey.setSecretKey(CLIENT_SECRET_MASK);
        return clientKey;
    }

    @Transactional
    public void deleteById(String id) {
        this.findById(id);
        clientDetailsServiceEntityRepository.deleteAllByKeyClientId(id);
        clientDetailsRepository.deleteById(id);
    }

    private ClientKey getRawClientKeyById(String clientId) {

        ClientKey clientKey = null;
        Optional<ClientDetailsEntity> clientDetailsEntityOptional = clientDetailsRepository.findById(clientId);
        if (clientDetailsEntityOptional.isPresent()) {
            List<ClientDetailsServiceEntity> serviceEntities = clientDetailsServiceEntityRepository.findAllByKeyClientId(clientId);
            clientKey = clientKeyConverter.convertClientDetailsEntity(clientDetailsEntityOptional.get(), serviceEntities);

        }
        return clientKey;
    }

}
