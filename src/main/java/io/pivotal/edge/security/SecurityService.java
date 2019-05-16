package io.pivotal.edge.security;

import io.pivotal.edge.EdgeRequestContext;
import io.pivotal.edge.keys.ClientKeyService;
import io.pivotal.edge.keys.web.ApplicationType;
import io.pivotal.edge.keys.web.ClientKey;
import io.pivotal.edge.keys.web.ClientService;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class SecurityService {

    private Optional<String> findClientService(String serviceId, Collection<String> allowedServices) {
        return allowedServices == null || serviceId == null ?
                Optional.empty() :
                allowedServices.stream().filter(s -> s.equalsIgnoreCase(serviceId)).findFirst();
    }

    public boolean validate(EdgeRequestContext edgeRequestContext) {

        if (ApplicationType.CONFIDENTIAL.name().equalsIgnoreCase(edgeRequestContext.getApplicationType()) && !StringUtils.equals(edgeRequestContext.getClientSecretKey(), edgeRequestContext.getRequestSecretKey())) {
            return false;
        }

        return this.findClientService(edgeRequestContext.getServiceId(), edgeRequestContext.getAllowedServices().keySet()).isPresent();
    }
}
