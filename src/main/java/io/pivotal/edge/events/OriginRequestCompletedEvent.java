package io.pivotal.edge.events;

import lombok.Builder;
import lombok.Value;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;

@Value
@Builder
public class OriginRequestCompletedEvent {

    private HttpServletRequest httpServletRequest;
    private HttpRequest httpRequest;
    private HttpServletResponse httpServletResponse;
    private HttpResponse httpResponse;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

}
