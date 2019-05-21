package io.pivotal.edge.keys.filters;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.util.HTTPRequestUtils;
import io.pivotal.edge.keys.ClientKeyConverter;
import io.pivotal.edge.keys.domain.ClientDetailsEntity;
import io.pivotal.edge.keys.domain.ClientDetailsEntityRepository;
import io.pivotal.edge.keys.domain.ClientDetailsServiceEntity;
import io.pivotal.edge.keys.domain.ClientDetailsServiceEntityRepository;
import io.pivotal.edge.keys.web.ClientKey;
import io.pivotal.edge.routing.EdgeRequestContext;
import io.pivotal.edge.servlet.filters.EdgeHttpServletRequestWrapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.security.jwt.Jwt;
import org.springframework.security.jwt.JwtHelper;
import org.springframework.stereotype.Service;
import org.springframework.util.Base64Utils;
import org.springframework.util.CollectionUtils;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.*;

import static io.pivotal.edge.EdgeApplicationConstants.API_KEY_PARAM;
import static io.pivotal.edge.EdgeApplicationConstants.REQUEST_ID_HEADER_NAME;

@Service
@Slf4j
public class ClientIdentityService {

    private ClientKeyCache clientKeyCache;

    private ClientKeyConverter clientKeyConverter;

    private ClientDetailsEntityRepository clientDetailsRepository;

    private ClientDetailsServiceEntityRepository clientDetailsServiceEntityRepository;

    private ObjectMapper objectMapper;

    public ClientIdentityService(ClientKeyCache clientKeyCache, ClientKeyConverter clientKeyConverter, ClientDetailsEntityRepository clientDetailsRepository, ClientDetailsServiceEntityRepository clientDetailsServiceEntityRepository, ObjectMapper objectMapper) {
        this.clientKeyCache = clientKeyCache;
        this.clientKeyConverter = clientKeyConverter;
        this.clientDetailsRepository = clientDetailsRepository;
        this.clientDetailsServiceEntityRepository = clientDetailsServiceEntityRepository;
        this.objectMapper = objectMapper;
    }

    public ClientKey findCachedClientKeyById(String clientId) {
        return clientKeyCache.findById(clientId).orElseGet(() -> {
            ClientKey clientKey = null;
            Optional<ClientDetailsEntity> clientDetailsEntityOptional = clientDetailsRepository.findById(clientId);
            if (clientDetailsEntityOptional.isPresent()) {
                List<ClientDetailsServiceEntity> serviceEntities = clientDetailsServiceEntityRepository.findAllByKeyClientId(clientId);
                clientKey = clientKeyConverter.convertClientDetailsEntity(clientDetailsEntityOptional.get(), serviceEntities);
                clientKeyCache.cache(clientKey);
            }
            return clientKey;
        });
    }

    public EdgeRequestContext createEdgeRequestContextFrom(RequestContext ctx) {

        HttpServletRequest httpServletRequest = ctx.getRequest();
        String authorizationHeader = (httpServletRequest == null ? null : httpServletRequest.getHeader(HttpHeaders.AUTHORIZATION));
        Map<String, List<String>> queryParams = HTTPRequestUtils.getInstance().getQueryParams();

        String clientId = null;
        String secretKey = null;
        String authorizationType = null;
        if (StringUtils.isNotBlank(authorizationHeader)) {
            String[] authSplit = authorizationHeader.split(" ");
            if (authSplit.length == 2) {
                authorizationType = authSplit[0];
            }
            if (StringUtils.equalsIgnoreCase("basic", authorizationType)) {
                String[] clientCreds = new String(Base64Utils.decodeFromString(authSplit[1].trim())).split(":");
                clientId = clientCreds[0];
                secretKey = clientCreds[1];
            } else if (StringUtils.equalsIgnoreCase("bearer", authorizationType)) {
                clientId = this.extractClientIdFromJwt(authSplit[1]);
            }
        } else if (!CollectionUtils.isEmpty(queryParams) && queryParams.containsKey(API_KEY_PARAM)) {
            clientId = queryParams.get(API_KEY_PARAM).get(0);
        }

        String requestId = (String) ctx.get(REQUEST_ID_HEADER_NAME);
        if (StringUtils.isBlank(requestId)) {
            EdgeHttpServletRequestWrapper requestWrapper = EdgeHttpServletRequestWrapper.extractFrom(httpServletRequest);
            requestId = (Objects.isNull(requestWrapper) ? null : requestWrapper.getRequestId());
        }

        EdgeRequestContext edgeRequestContext = new EdgeRequestContext();
        edgeRequestContext.setRequestId(requestId);
        edgeRequestContext.setClientId(clientId);
        edgeRequestContext.setRequestSecretKey(secretKey);
        edgeRequestContext.setAuthorizationType(authorizationType);
        return edgeRequestContext;
    }

    private String extractClientIdFromJwt(String token)  {

        Jwt jwt = JwtHelper.decode(token);
        String claims = jwt.getClaims();
        try {
            HashMap<String, Object> claimsMap = objectMapper.readValue(claims, HashMap.class);
            return (String) claimsMap.get("client_id");
        } catch (IOException e) {
            log.warn("Error Occurred Parsing JWT Claims {}", e);
        }
        return null;
    }
}
