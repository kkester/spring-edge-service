package io.pivotal.edge.keys.web;

import io.pivotal.edge.keys.ClientKeyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.util.UriComponentsBuilder;

import javax.validation.Valid;
import java.net.URI;
import java.util.Collection;

@RestController
@RequestMapping(value = "/client-keys")
public class ClientKeyController {

    @Autowired
    private ClientKeyService clientKeyService;

    @GetMapping
    public Collection<ClientKey> getClientKeys() {
        return clientKeyService.findAll();
    }

    @GetMapping(value = "/{id}")
    public ClientKey getClientKey(@PathVariable String id) throws NoHandlerFoundException {
        return clientKeyService.findById(id);
    }

    @PostMapping
    public ResponseEntity<ClientKey> createClientKey(@RequestBody @Valid ClientKey clientKeyData, UriComponentsBuilder uriBuilder) {

        ClientKey clientKey = clientKeyService.createClientKey(clientKeyData);
        URI location = uriBuilder.path("/client-keys/{0}").build(clientKey.getClientId());
        return ResponseEntity.status(HttpStatus.CREATED).location(location).body(clientKey);
    }

    @DeleteMapping(value = "/{id}")
    public void deleteClientKey(@PathVariable String id) throws NoHandlerFoundException {
        clientKeyService.deleteById(id);
    }

}
