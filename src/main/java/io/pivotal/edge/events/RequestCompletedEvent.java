package io.pivotal.edge.events;

import io.pivotal.edge.servlet.filters.EdgeHttpServletRequestWrapper;
import lombok.Builder;
import lombok.Value;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;

@Value
@Builder
public class RequestCompletedEvent {

    private EdgeHttpServletRequestWrapper httpServletRequest;
    private HttpServletResponse httpServletResponse;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

}
