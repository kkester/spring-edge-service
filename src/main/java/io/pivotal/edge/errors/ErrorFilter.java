package io.pivotal.edge.errors;

import com.netflix.client.ClientException;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.conn.HttpHostConnectException;
import org.springframework.cloud.netflix.zuul.util.ZuulRuntimeException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.net.SocketTimeoutException;

import static org.springframework.cloud.netflix.zuul.filters.support.FilterConstants.ERROR_TYPE;
import static org.springframework.cloud.netflix.zuul.filters.support.FilterConstants.SEND_ERROR_FILTER_ORDER;

@Component
@Slf4j
public class ErrorFilter extends ZuulFilter {

    @Override
    public String filterType() {
        return ERROR_TYPE;
    }

    @Override
    public int filterOrder() {
        return SEND_ERROR_FILTER_ORDER;
    }

    @Override
    public boolean shouldFilter() {
        RequestContext ctx = RequestContext.getCurrentContext();
        // only forward to errorPath if it hasn't been forwarded to already
        return ctx.getThrowable() != null;
    }

    @Override
    public Object run() {

        log.info("Executing Error Filter");

        RequestContext ctx = RequestContext.getCurrentContext();
        ExceptionHolder exception = findZuulException(ctx.getThrowable());
        ctx.setResponseStatusCode(exception.getStatusCode());

        return null;
    }

    protected ExceptionHolder findZuulException(Throwable throwable) {
        if (throwable.getCause() instanceof ZuulRuntimeException) {
            Throwable cause = null;
            if (throwable.getCause().getCause() != null) {
                cause = throwable.getCause().getCause().getCause();
            }
            if (cause instanceof ClientException && cause.getCause() != null
                    && cause.getCause().getCause() instanceof SocketTimeoutException) {

                ZuulException zuulException = new ZuulException("", 504,
                        ZuulException.class.getName() + ": Hystrix Readed time out");
                return new ZuulExceptionHolder(zuulException);
            }
            if (cause instanceof HttpHostConnectException) {
                ZuulException zuulException = new ZuulException("", 503,
                        ZuulException.class.getName() + ": Origin ClientService Unavailable");
                return new ZuulExceptionHolder(zuulException);
            }
            // this was a failure initiated by one of the local filters
            else if (throwable.getCause().getCause() instanceof ZuulException) {
                return new ZuulExceptionHolder(
                        (ZuulException) throwable.getCause().getCause());
            }
        }

        if (throwable.getCause() instanceof ZuulException) {
            // wrapped zuul exception
            return new ZuulExceptionHolder((ZuulException) throwable.getCause());
        }

        if (throwable instanceof ZuulException) {
            // exception thrown by zuul lifecycle
            return new ZuulExceptionHolder((ZuulException) throwable);
        }

        // fallback
        return new DefaultExceptionHolder(throwable);
    }

    protected interface ExceptionHolder {

        Throwable getThrowable();

        default int getStatusCode() {
            return HttpStatus.INTERNAL_SERVER_ERROR.value();
        }

        default String getErrorCause() {
            return null;
        }

    }

    protected static class DefaultExceptionHolder implements ExceptionHolder {

        private final Throwable throwable;

        public DefaultExceptionHolder(Throwable throwable) {
            this.throwable = throwable;
        }

        @Override
        public Throwable getThrowable() {
            return this.throwable;
        }

    }

    protected static class ZuulExceptionHolder implements ExceptionHolder {

        private final ZuulException exception;

        public ZuulExceptionHolder(ZuulException exception) {
            this.exception = exception;
        }

        @Override
        public Throwable getThrowable() {
            return this.exception;
        }

        @Override
        public int getStatusCode() {
            return this.exception.nStatusCode;
        }

        @Override
        public String getErrorCause() {
            return this.exception.errorCause;
        }

    }
}
