package io.pivotal.edge.security;

import com.netflix.zuul.context.RequestContext;
import lombok.Data;
import org.apache.commons.lang.StringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.util.Base64Utils;

import javax.servlet.http.HttpServletRequest;

@Data
public class ClientSecretCredentials {

    private static final String API_KEY = "apiKey";

    private String clientKey;
    private String secretKey;
    private String realm;

    public static ClientSecretCredentials createFrom(RequestContext ctx) {

        HttpServletRequest httpServletRequest = ctx.getRequest();
        String authorizationHeader = (httpServletRequest == null ? null : httpServletRequest.getHeader(HttpHeaders.AUTHORIZATION));

        ClientSecretCredentials clientSecretCredentials = null;
        String apiKeyQueryParam = httpServletRequest.getParameter(API_KEY);
        if (StringUtils.isNotBlank(authorizationHeader)) {
            String[] authSplit = authorizationHeader.split(" ");
            String[] clientCreds = new String(Base64Utils.decodeFromString(authSplit[1].trim())).split(":");
            if (clientCreds.length == 2) {
                clientSecretCredentials = new ClientSecretCredentials(clientCreds[0], clientCreds[1], authSplit[0]);
            }
        } else if (StringUtils.isNotBlank(apiKeyQueryParam)){
            clientSecretCredentials = new ClientSecretCredentials(apiKeyQueryParam, null, null);
        }

        return clientSecretCredentials;
    }

    private ClientSecretCredentials(String clientKey, String secretKey, String realm) {
        this.clientKey = clientKey;
        this.secretKey = secretKey;
        this.realm = realm;
    }

}
