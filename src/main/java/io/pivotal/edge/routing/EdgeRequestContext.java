package io.pivotal.edge.routing;

import lombok.Data;

import java.util.Map;

@Data
public class EdgeRequestContext {

    private String clientId;
    private String requestId;
    private String clientSecretKey;
    private String requestSecretKey;
    private String serviceId;
    private String authorizationType;
    private String applicationType;
    private Map<String, String> allowedServices;

}
