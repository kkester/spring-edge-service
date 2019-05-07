package integration.core;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

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
        String url = this.edgeServiceAppHost + currentRequest.getUrl() + "?apiKey=" + currentRequest.getClientKey().getId();
        HttpHeaders headers = new HttpHeaders();
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
