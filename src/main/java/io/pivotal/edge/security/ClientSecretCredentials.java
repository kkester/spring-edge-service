package io.pivotal.edge.security;

import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.util.HTTPRequestUtils;
import io.pivotal.edge.keys.ClientKey;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.util.Base64Utils;
import org.springframework.util.CollectionUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static io.pivotal.edge.EdgeApplicationConstants.API_KEY_PARAM;
import static io.pivotal.edge.EdgeApplicationConstants.CLIENT_KEY;

@Data
@Slf4j
public class ClientSecretCredentials {

    private String clientKey;
    private String secretKey;
    private String realm;

    public static ClientSecretCredentials createFrom(RequestContext ctx) {

        HttpServletRequest httpServletRequest = ctx.getRequest();
        String authorizationHeader = (httpServletRequest == null ? null : httpServletRequest.getHeader(HttpHeaders.AUTHORIZATION));
        log.info("Autth Header: {}", authorizationHeader);

        ClientSecretCredentials clientSecretCredentials = null;
        Map<String, List<String>> queryParams = HTTPRequestUtils.getInstance().getQueryParams();
        log.info("Query String: {}", queryParams);
        ClientKey clientKey = (ClientKey) ctx.get(CLIENT_KEY);
        if (StringUtils.isNotBlank(authorizationHeader)) {
            String[] authSplit = authorizationHeader.split(" ");
            String[] clientCreds = new String(Base64Utils.decodeFromString(authSplit[1].trim())).split(":");
            if (clientCreds.length == 2) {
                clientSecretCredentials = new ClientSecretCredentials(clientCreds[0], clientCreds[1], authSplit[0]);
            }
        } else if (!CollectionUtils.isEmpty(queryParams) && queryParams.containsKey(API_KEY_PARAM)){
            clientSecretCredentials = new ClientSecretCredentials(queryParams.get(API_KEY_PARAM).get(0), null, null);
        } else if (Objects.nonNull(clientKey)) {
            clientSecretCredentials = new ClientSecretCredentials(clientKey.getId(), clientKey.getSecretKey(), null);
        }

        return clientSecretCredentials;
    }

    private ClientSecretCredentials(String clientKey, String secretKey, String realm) {
        this.clientKey = clientKey;
        this.secretKey = secretKey;
        this.realm = realm;
    }

}
