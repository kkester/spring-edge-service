package io.pivotal.edge.security;

import com.netflix.zuul.context.RequestContext;
import lombok.Data;
import org.apache.commons.lang.StringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.util.Base64Utils;
import org.springframework.util.CollectionUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Data
public class ClientSecretCredentials {

    private static final String API_KEY = "apiKey";

    private String clientKey;
    private String secretKey;

    public static ClientSecretCredentials createFrom(RequestContext ctx) {

        HttpServletRequest httpServletRequest = ctx.getRequest();
        String authorizationHeader = (httpServletRequest == null ? null : httpServletRequest.getHeader(HttpHeaders.AUTHORIZATION));
        authorizationHeader = StringUtils.stripStart(authorizationHeader, "Basic");

        ClientSecretCredentials clientSecretCredentials = null;

        List<String> apiKeyQueryParam = ctx.getRequestQueryParams().get(API_KEY);
        if (StringUtils.isNotBlank(authorizationHeader)) {
            String[] clientCreds = new String(Base64Utils.decodeFromString(authorizationHeader.trim())).split(":");
            if (clientCreds.length == 2) {
                clientSecretCredentials = new ClientSecretCredentials(clientCreds[0], clientCreds[1]);
            }
        } else if (!CollectionUtils.isEmpty(apiKeyQueryParam)){
            clientSecretCredentials = new ClientSecretCredentials(apiKeyQueryParam.get(0), null);
        }

        return clientSecretCredentials;
    }

    private ClientSecretCredentials(String clientKey, String secretKey) {
        this.clientKey = clientKey;
        this.secretKey = secretKey;
    }

}
