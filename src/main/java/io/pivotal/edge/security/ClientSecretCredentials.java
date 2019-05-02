package io.pivotal.edge.security;

import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.util.HTTPRequestUtils;
import lombok.Data;
import org.apache.commons.lang.StringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.util.Base64Utils;
import org.springframework.util.CollectionUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

import static io.pivotal.edge.EdgeApplicationConstants.API_KEY;

@Data
public class ClientSecretCredentials {

    private String clientKey;
    private String secretKey;
    private String realm;

    public static ClientSecretCredentials createFrom(RequestContext ctx) {

        HttpServletRequest httpServletRequest = ctx.getRequest();
        String authorizationHeader = (httpServletRequest == null ? null : httpServletRequest.getHeader(HttpHeaders.AUTHORIZATION));

        ClientSecretCredentials clientSecretCredentials = null;
        Map<String, List<String>> queryParams = HTTPRequestUtils.getInstance().getQueryParams();
        String apiKey = (String) ctx.get(API_KEY);
        if (StringUtils.isNotBlank(authorizationHeader)) {
            String[] authSplit = authorizationHeader.split(" ");
            String[] clientCreds = new String(Base64Utils.decodeFromString(authSplit[1].trim())).split(":");
            if (clientCreds.length == 2) {
                clientSecretCredentials = new ClientSecretCredentials(clientCreds[0], clientCreds[1], authSplit[0]);
            }
        } else if (!CollectionUtils.isEmpty(queryParams) && queryParams.containsKey(API_KEY)){
            clientSecretCredentials = new ClientSecretCredentials(queryParams.get(API_KEY).get(0), null, null);
        } else if (StringUtils.isNotBlank(apiKey)) {
            clientSecretCredentials = new ClientSecretCredentials(apiKey, null, null);
        }

        return clientSecretCredentials;
    }

    private ClientSecretCredentials(String clientKey, String secretKey, String realm) {
        this.clientKey = clientKey;
        this.secretKey = secretKey;
        this.realm = realm;
    }

}
