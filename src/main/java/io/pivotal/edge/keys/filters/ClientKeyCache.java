package io.pivotal.edge.keys.filters;

import io.pivotal.edge.keys.web.ClientKey;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Component
public class ClientKeyCache {

    private Map<String, ClientKey> clientKeyCache = new HashMap<>();

    public Optional<ClientKey> findById(String clientId) {
        ClientKey clientKey = clientKeyCache.get(clientId);
        return Optional.ofNullable(clientKey);
    }

    public void cache(ClientKey clientKey) {
        clientKeyCache.put(clientKey.getClientId(), clientKey);
    }

    public void deleteById(String clientId) {
        clientKeyCache.remove(clientId);
    }
}
