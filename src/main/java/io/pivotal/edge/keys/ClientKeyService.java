package io.pivotal.edge.keys;

import io.pivotal.edge.security.ClientSecretCredentials;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

@Service
public class ClientKeyService {

    @Autowired
    private ClientKeyRepository clientKeyRepository;

    public Collection<ClientKey> findAll() {
        return clientKeyRepository.findAll();
    }

    public ClientKey createClientKey(ClientKey clientKeyData) {

        String clientKey = UUID.randomUUID().toString().replace("-", "");
        String secretKey = UUID.randomUUID().toString().replace("-", "");

        clientKeyData.setId(clientKey);
        clientKeyData.setSecretKey(secretKey);
        clientKeyData.setCreatedOn(LocalDateTime.now());
        clientKeyData.setLastUpdated(LocalDateTime.now());

        clientKeyRepository.save(clientKeyData);

        return clientKeyData;
    }

    public ClientKey findById(String id) throws NoHandlerFoundException {
        Optional<ClientKey> clientKeyOptional = clientKeyRepository.findById(id);
        if (!clientKeyOptional.isPresent()) {
            throw new NoHandlerFoundException(HttpMethod.GET.name(), id, new HttpHeaders());
        }
        return clientKeyOptional.get();
    }

    public void deleteById(String id) {
        clientKeyRepository.deleteById(id);
    }

}
