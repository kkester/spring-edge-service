package io.pivotal.edge.auditing;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuditLogRecordRepository extends ElasticsearchRepository<AuditLogRecord, String> {

}
