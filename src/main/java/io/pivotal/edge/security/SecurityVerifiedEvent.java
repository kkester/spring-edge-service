package io.pivotal.edge.security;

import lombok.Builder;
import lombok.Value;

import javax.servlet.http.HttpServletRequest;

@Value
@Builder
public class SecurityVerifiedEvent {

    private HttpServletRequest request;
    private String clientKey;

}
