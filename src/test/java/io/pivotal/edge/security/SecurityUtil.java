package io.pivotal.edge.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.pivotal.edge.keys.domain.ClientDetailsEntity;
import io.pivotal.edge.keys.web.ClientKey;
import org.springframework.security.jwt.JwtHelper;
import org.springframework.security.jwt.crypto.sign.MacSigner;
import org.springframework.util.Base64Utils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

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

    public static String createBearerTokenFrom(String clientKey) {

        String bearerToken = null;
        Map<String,Object> claims = new HashMap<>();
        claims.put("client_id", clientKey);
        try {
            bearerToken = JwtHelper.encode(new ObjectMapper().writeValueAsString(claims), new MacSigner("MaYzkSjmkzPC57L")).getEncoded();
        } catch (IOException e) {
            // continue
        }
        return bearerToken;
    }
}
