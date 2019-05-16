package io.pivotal.edge;

import lombok.Data;

import java.util.Map;

@Data
public class EdgeRequestContext {

    private String clientId;
    private String requestId;
    private String clientSecretKey;
    private String requestSecretKey;
    private String serviceId;
    private String realm;
    private String applicationType;
    private Map<String, String> allowedServices;

}