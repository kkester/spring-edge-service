package io.pivotal.edge.auditing;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpMethod;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogRecord {

    private String id;
    private HttpMethod method;
    private String requestUri;
    private Integer httpStatus;
    private Integer originHttpStatus;
    private String serviceId;
    private String clientKey;
    private LocalDateTime requestDate;
    private Long executionTimeMillis;
    private Long originExecutionTimeMillis;

}
