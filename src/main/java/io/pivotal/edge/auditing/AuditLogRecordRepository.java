package io.pivotal.edge.auditing;

import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
public class AuditLogRecordRepository {

    private Map<String, AuditLogRecord> logRecords = new HashMap<>();


    public Collection<AuditLogRecord> findAll() {
        return logRecords.values();
    }

    public void save(AuditLogRecord record) {
        String requestId = UUID.randomUUID().toString();
        record.setId(requestId);
        logRecords.put(record.getId(), record);
    }

    public AuditLogRecord findById(String id) {
        return logRecords.get(id);
    }
}
