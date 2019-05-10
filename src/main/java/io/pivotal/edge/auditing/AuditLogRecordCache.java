package io.pivotal.edge.auditing;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class AuditLogRecordCache {

    private AuditLogRecordRepository auditLogRecordRepository;

    private Map<String, AuditLogRecord> auditLogRecordCache = new HashMap<>();

    public AuditLogRecordCache(AuditLogRecordRepository auditLogRecordRepository) {
        this.auditLogRecordRepository = auditLogRecordRepository;
    }

    public void cache(AuditLogRecord record) {
        auditLogRecordCache.put(record.getId(), record);
    }

    public AuditLogRecord findById(String requestId) {
        return auditLogRecordCache.get(requestId);
    }

    public void save(AuditLogRecord logRecord) {
        auditLogRecordRepository.save(logRecord);
        auditLogRecordCache.remove(logRecord.getId());
    }

}
