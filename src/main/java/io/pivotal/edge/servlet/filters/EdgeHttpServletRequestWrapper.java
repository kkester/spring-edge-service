package io.pivotal.edge.servlet.filters;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

public class EdgeHttpServletRequestWrapper extends HttpServletRequestWrapper {

    private String requestId;

    public EdgeHttpServletRequestWrapper(HttpServletRequest request) {
        super(request);
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }
}
