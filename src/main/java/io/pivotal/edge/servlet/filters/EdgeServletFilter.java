package io.pivotal.edge.servlet.filters;

import io.pivotal.edge.events.EventPublisher;
import io.pivotal.edge.events.RequestCompletedEvent;
import io.pivotal.edge.events.RequestInitiatedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;

@Component
@Order(1)
@Slf4j
public class EdgeServletFilter implements Filter {

    @Autowired
    private EventPublisher eventPublisher;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        LocalDateTime start = LocalDateTime.now();

        EdgeHttpServletRequestWrapper requestWrapper = new EdgeHttpServletRequestWrapper((HttpServletRequest) request);
        log.info("Starting handling req : {}", requestWrapper.getRequestURI());
        eventPublisher.publishEvent(RequestInitiatedEvent.builder().httpServletRequest(requestWrapper).initiatedTime(start).build());

        chain.doFilter(requestWrapper, response);

        LocalDateTime end = LocalDateTime.now();
        eventPublisher.publishEvent(RequestCompletedEvent.builder().httpServletRequest(requestWrapper).httpServletResponse((HttpServletResponse) response).startTime(start).endTime(end).build());

        log.info("Completed handling req: {}; status: {}", requestWrapper.getRequestURI(), ((HttpServletResponse) response).getStatus());
    }

}
