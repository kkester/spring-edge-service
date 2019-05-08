package integration.core;

import io.pivotal.edge.keys.ApplicationType;
import io.pivotal.edge.keys.ClientKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static io.pivotal.edge.security.SecurityUtil.base64EncodeClientCredentials;

@Component
@Slf4j
public class RestApiFeature {

    private ThreadLocal<ApiRequest> currentRequest = new ThreadLocal<>();

    private List<ResponseResults> responses = new ArrayList<>();

    @Value("${application.integration.edge-service-app-host}")
    private String edgeServiceAppHost;

    @Autowired
    private RestTemplateInvoker restTemplateInvoker;

    @Async
    public void getResource(ApiRequest currentRequest) {

        String url = this.edgeServiceAppHost + currentRequest.getUrl();
        ClientKey clientKey = currentRequest.getClientKey();
        HttpHeaders headers = new HttpHeaders();
        if (ApplicationType.PUBLIC.equals(clientKey.getApplicationType())) {
            url = url + "?apiKey=" + clientKey.getId();
        } else {
            String basicCredentials = "basic " + base64EncodeClientCredentials(clientKey);
            headers.add(HttpHeaders.AUTHORIZATION, basicCredentials);
        }

        ResponseResults results = restTemplateInvoker.get(url, headers);
        this.logResults(results);
        responses.add(results);
    }

    private void logResults(ResponseResults results) {
        try {
            log.info("Request executed with status {} and request id of {}", results.getResponse().getStatusCode(), results.getResponse().getHeaders().get("x-request-id"));
        } catch (Exception e) {
            log.info("Unable to log results");
        }
    }

    public List<ResponseResults> getResponses() {
        return responses;
    }

    public ApiRequest getCurrentRequest() {
        return currentRequest.get();
    }

    public void setCurrentRequest(ApiRequest currentRequest) {
        this.currentRequest.set(currentRequest);
    }
}
