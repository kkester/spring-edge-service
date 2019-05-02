package io.pivotal.edge.events;

import io.pivotal.edge.servlet.filters.EdgeHttpServletRequestWrapper;
import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;

@Value
@Builder
public class RequestInitiatedEvent {

    private EdgeHttpServletRequestWrapper httpServletRequest;
    private LocalDateTime initiatedTime;

}
