package integration.core;

import io.pivotal.edge.keys.web.ClientKey;
import lombok.Data;
import org.springframework.http.HttpHeaders;

@Data
public class ApiRequest {

    private String url;
    private HttpHeaders headers;
    private String body;
    private ClientKey clientKey;

}
