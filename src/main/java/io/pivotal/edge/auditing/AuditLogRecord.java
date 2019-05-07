package io.pivotal.edge.auditing;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.http.HttpMethod;

import javax.persistence.Id;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(indexName = "edgeservice-v2", type = "auditlogrecords")
public class AuditLogRecord {

    @Id
    private String id;
    private HttpMethod method;
    private String requestUri;
    private Integer httpStatus;
    private Integer originHttpStatus;
    private String serviceId;
    private String clientKey;
    private String requestDate;
    private Long executionTimeMillis;
    private Long originExecutionTimeMillis;

}
