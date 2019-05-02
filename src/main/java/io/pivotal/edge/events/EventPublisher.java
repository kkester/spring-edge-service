package io.pivotal.edge.events;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.PayloadApplicationEvent;
import org.springframework.stereotype.Component;

@Component
public class EventPublisher {

    private ApplicationEventPublisher eventPublisher;

    public EventPublisher(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    public void publishEvent(Object payload) {
        eventPublisher.publishEvent(new PayloadApplicationEvent<>(this, payload));
    }

}
