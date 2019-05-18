package io.pivotal.edge.keys.filters;

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
import org.apache.commons.lang.StringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.util.Base64Utils;
import org.springframework.util.CollectionUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

import static io.pivotal.edge.EdgeApplicationConstants.API_KEY_PARAM;
import static io.pivotal.edge.EdgeApplicationConstants.REQUEST_ID_HEADER_NAME;

@Service
public class ClientIdentityService {

    private ClientKeyCache clientKeyCache;

    private ClientKeyConverter clientKeyConverter;

    private ClientDetailsEntityRepository clientDetailsRepository;

    private ClientDetailsServiceEntityRepository clientDetailsServiceEntityRepository;

    public ClientIdentityService(ClientKeyCache clientKeyCache, ClientKeyConverter clientKeyConverter, ClientDetailsEntityRepository clientDetailsRepository, ClientDetailsServiceEntityRepository clientDetailsServiceEntityRepository) {
        this.clientKeyCache = clientKeyCache;
        this.clientKeyConverter = clientKeyConverter;
        this.clientDetailsRepository = clientDetailsRepository;
        this.clientDetailsServiceEntityRepository = clientDetailsServiceEntityRepository;
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
        String realm = null;
        if (StringUtils.isNotBlank(authorizationHeader)) {
            String[] authSplit = authorizationHeader.split(" ");
            String[] clientCreds = new String(Base64Utils.decodeFromString(authSplit[1].trim())).split(":");
            if (clientCreds.length == 2) {
                clientId = clientCreds[0];
                secretKey = clientCreds[1];
                realm = authSplit[0];
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
        edgeRequestContext.setRealm(realm);
        return edgeRequestContext;
    }
}
