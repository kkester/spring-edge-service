package io.pivotal.edge.security;

import io.pivotal.edge.keys.ClientKey;
import org.springframework.util.Base64Utils;

public class SecurityUtil {

    public static String base64EncodeClientCredentials(String clientKey, String secretKey) {
        String credentials = clientKey + ":" + secretKey;
        return new String(Base64Utils.encode(credentials.getBytes()));
    }

    public static String base64EncodeClientCredentials(ClientKey clientKey) {
        return base64EncodeClientCredentials(clientKey.getId(), clientKey.getSecretKey());
    }

}
