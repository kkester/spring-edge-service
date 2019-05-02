package io.pivotal.edge.auditing;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;

@RestController
public class AuditLogRecordController {

    @Autowired
    private AuditingService auditingService;

    @GetMapping(value = "/audit-logs")
    public Collection<AuditLogRecord> getAuditLogRecords() {
        return auditingService.getAuditLogRecords();
    }

}
