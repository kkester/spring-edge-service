package integration.core;

import io.pivotal.edge.keys.ClientKey;
import io.pivotal.edge.security.ClientSecretCredentials;
import lombok.Builder;
import lombok.Data;
import lombok.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;

@Data
public class ApiRequest {

    private String url;
    private HttpHeaders headers;
    private String body;
    private ClientKey clientKey;

}
