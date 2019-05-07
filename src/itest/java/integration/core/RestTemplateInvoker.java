package integration.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.Objects;

@Component
public class RestTemplateInvoker {

    @Autowired
    private RestTemplateBuilder restTemplateBuilder;

    @Autowired
    private ObjectMapper objectMapper;

    public ResponseResults get(String url, HttpHeaders headers) {

        ResponseResultErrorHandler errorHandler = new ResponseResultErrorHandler();
        RestTemplate restTemplate = restTemplateBuilder.errorHandler(errorHandler).build();
        return restTemplate.execute(url,
                HttpMethod.GET,
                request -> {
                    HttpHeaders requestHeaders = request.getHeaders();
                    if (!Objects.isNull(headers)) {
                        requestHeaders.addAll(headers);
                    }
                },
                response -> {
                    if (errorHandler.hadError) {
                        return (errorHandler.getResults());
                    } else {
                        return (new ResponseResults(response, objectMapper));
                    }
                });
    }

    public ResponseResults post(ApiRequest apiRequest) {

        ResponseResultErrorHandler errorHandler = new ResponseResultErrorHandler();
        RestTemplate restTemplate = restTemplateBuilder.errorHandler(errorHandler).build();
        return restTemplate.execute(apiRequest.getUrl(), HttpMethod.POST,
                request -> {
                    HttpHeaders requestHeaders = request.getHeaders();
                    if (!Objects.isNull(apiRequest.getHeaders())) {
                        requestHeaders.addAll(apiRequest.getHeaders());
                    }
                    new StringHttpMessageConverter().write(apiRequest.getBody(), MediaType.APPLICATION_JSON, request);
                },
                response -> {
                    if (errorHandler.hadError) {
                        return (errorHandler.getResults());
                    } else {
                        return (new ResponseResults(response, objectMapper));
                    }
                });
    }

    private class ResponseResultErrorHandler implements ResponseErrorHandler {

        private ResponseResults results = null;
        private Boolean hadError = false;

        private ResponseResults getResults() {
            return results;
        }

        @Override
        public boolean hasError(ClientHttpResponse response) throws IOException {
            hadError = response.getRawStatusCode() >= 400;
            return hadError;
        }

        @Override
        public void handleError(ClientHttpResponse response) throws IOException {
            results = new ResponseResults(response, objectMapper);
        }
    }
}
