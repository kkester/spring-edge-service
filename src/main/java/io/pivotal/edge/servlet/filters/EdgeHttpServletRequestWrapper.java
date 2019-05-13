package io.pivotal.edge.servlet.filters;

import org.springframework.http.HttpHeaders;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Objects;

public class EdgeHttpServletRequestWrapper extends HttpServletRequestWrapper {

    private String requestId;

    private HttpHeaders headers;

    public static EdgeHttpServletRequestWrapper extractFrom(HttpServletRequest request) {
        EdgeHttpServletRequestWrapper edgeRequestWrapper = null;
        if (request instanceof EdgeHttpServletRequestWrapper) {
            edgeRequestWrapper = (EdgeHttpServletRequestWrapper) request;
        } else if (request instanceof com.netflix.zuul.http.HttpServletRequestWrapper) {
            com.netflix.zuul.http.HttpServletRequestWrapper requestWrapper = (com.netflix.zuul.http.HttpServletRequestWrapper)request;
            if (requestWrapper.getRequest() instanceof EdgeHttpServletRequestWrapper) {
                edgeRequestWrapper = (EdgeHttpServletRequestWrapper) requestWrapper.getRequest();
            }
        }
        return edgeRequestWrapper;
    }

    public EdgeHttpServletRequestWrapper(HttpServletRequest request) {
        super(request);
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public HttpHeaders getHeaders() {
        return headers;
    }

    public void setHeaders(HttpHeaders headers) {
        this.headers = headers;
    }

    @Override
    public String getHeader(String name) {
        return (headers == null ? super.getHeader(name) : headers.getFirst(name));
    }

    @Override
    public Enumeration<String> getHeaderNames() {
        return (headers == null ? super.getHeaderNames() : Collections.enumeration(headers.keySet()));
    }

    public void remove (String name) {
        if (Objects.isNull(headers)) {
            this.establishHeaders();
        }
        headers.remove(name);
    }

    private void establishHeaders() {
        headers = new HttpHeaders();
        Enumeration<String> headerNames = super.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            Enumeration<String> headerValues = super.getHeaders(headerName);
            headers.put(headerName, Collections.list(headerValues));
        }
    }
}
