package io.pivotal.edge.security;

import io.pivotal.edge.routing.EdgeRequestContext;
import io.pivotal.edge.keys.web.ApplicationType;
import org.apache.commons.lang.StringUtils;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SecurityService {

    private Optional<String> findClientService(String serviceId, Collection<String> allowedServices) {
        return allowedServices == null || serviceId == null ?
                Optional.empty() :
                allowedServices.stream().filter(s -> s.equalsIgnoreCase(serviceId)).findFirst();
    }

    public boolean validate(EdgeRequestContext edgeRequestContext) {
        return this.findClientService(edgeRequestContext.getServiceId(), edgeRequestContext.getAllowedServices().keySet()).isPresent();
    }

}
