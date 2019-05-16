package io.pivotal.edge.events;

import lombok.Builder;
import lombok.Value;

import javax.servlet.http.HttpServletRequest;

@Value
@Builder
public class ClientIdentifiedEvent {

    private HttpServletRequest request;
    private String clientKey;
    private String requestId;

}
