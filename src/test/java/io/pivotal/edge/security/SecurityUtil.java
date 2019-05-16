package io.pivotal.edge.security;

import io.pivotal.edge.keys.domain.ClientDetailsEntity;
import io.pivotal.edge.keys.web.ClientKey;
import org.springframework.util.Base64Utils;

public class SecurityUtil {

    public static String base64EncodeClientCredentials(String clientKey, String secretKey) {
        String credentials = clientKey + ":" + secretKey;
        return new String(Base64Utils.encode(credentials.getBytes()));
    }

    public static String base64EncodeClientCredentials(ClientKey clientKey) {
        return base64EncodeClientCredentials(clientKey.getClientId(), clientKey.getSecretKey());
    }

    public static String base64EncodeClientCredentials(ClientDetailsEntity clientKey) {
        return base64EncodeClientCredentials(clientKey.getClientId(), clientKey.getClientSecret());
    }

}
