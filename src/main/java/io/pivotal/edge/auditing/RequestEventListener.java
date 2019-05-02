package io.pivotal.edge.auditing;

import io.pivotal.edge.events.OriginRequestCompletedEvent;
import io.pivotal.edge.events.RequestCompletedEvent;
import io.pivotal.edge.events.RequestInitiatedEvent;
import io.pivotal.edge.security.SecurityVerifiedEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationListener;
import org.springframework.context.PayloadApplicationEvent;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "zuul.RequestIdFilter.pre", name = "disable", havingValue = "false")
public class RequestEventListener implements ApplicationListener<PayloadApplicationEvent<?>> {

    @Autowired
    private AuditingService auditingService;

    @Override
    public void onApplicationEvent(PayloadApplicationEvent<?> event) {

        Object requestEvent = event.getPayload();
        if (requestEvent instanceof RequestInitiatedEvent) {
            auditingService.createAuditLogRecordFrom((RequestInitiatedEvent)requestEvent);
        } else if (requestEvent instanceof OriginRequestCompletedEvent) {
            auditingService.updateAuditLogRecordForPostOriginFrom((OriginRequestCompletedEvent)requestEvent);
        } else if (requestEvent instanceof RequestCompletedEvent) {
            auditingService.finalizeAuditLogRecordFrom((RequestCompletedEvent)requestEvent);
        } else if (requestEvent instanceof SecurityVerifiedEvent) {
            auditingService.updateAuditLogRecordForSecurityVerifiedFrom((SecurityVerifiedEvent)requestEvent);
        }
    }

}
