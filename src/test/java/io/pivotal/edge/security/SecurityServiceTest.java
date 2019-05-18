package io.pivotal.edge.security;

import io.pivotal.edge.routing.EdgeRequestContext;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SecurityServiceTest {

    private static final String VALID_SERVICE_ID = "VALID_SERVICE_ID";

    private static final String INVALID_SERVICE_ID = "INVALID_SERVICE_ID";

    private SecurityService subject;

    private Map<String, String> allowedServices = new HashMap<>();

    @Before
    public void setUp() {
        subject = new SecurityService();
    }

    @Test
    public void testValidatePublicClientKey() {
        // given
        allowedServices.put(VALID_SERVICE_ID, null);
        EdgeRequestContext edgeRequestContext = new EdgeRequestContext();
        edgeRequestContext.setApplicationType("public");
        edgeRequestContext.setServiceId(VALID_SERVICE_ID);
        edgeRequestContext.setAllowedServices(allowedServices);

        // when
        boolean result = subject.validate(edgeRequestContext);

        // then
        assertThat(result).isTrue();
    }

    @Test
    public void testValidateInvalidPublicClientKey() {
        // given
        allowedServices.put(VALID_SERVICE_ID, null);
        EdgeRequestContext edgeRequestContext = new EdgeRequestContext();
        edgeRequestContext.setApplicationType("public");
        edgeRequestContext.setServiceId(INVALID_SERVICE_ID);
        edgeRequestContext.setAllowedServices(allowedServices);

        // when
        boolean result = subject.validate(edgeRequestContext);

        // then
        assertThat(result).isFalse();
    }

}