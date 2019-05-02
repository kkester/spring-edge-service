package io.pivotal.edge;

import com.github.tomakehurst.wiremock.WireMockServer;
import io.pivotal.edge.auditing.AuditLogRecord;
import io.pivotal.edge.auditing.AuditLogRecordRepository;
import io.pivotal.edge.keys.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.util.Base64Utils;

import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles(profiles = {"test", "wire"})
@ContextConfiguration(initializers = ApplicationTestContextInitializer.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD) // Needed so each test gets its own WireMock Server
public class EdgeApplicationTest {

	@Autowired
	private WireMockServer wireMockServer;

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private AuditLogRecordRepository auditLogRecordRepository;

	@MockBean
	private ClientKeyRepository clientKeyRepository;

	private ClientKey clientKey;

	@Test
	public void shouldRouteRequestForPublicClientKey_WhenGivenValidCredentials() throws Exception {

		// given
		givenThat(get(urlEqualTo("/resource"))
				.willReturn(aResponse().withStatus(200)));

		// when
		RequestBuilder requestBuilder = MockMvcRequestBuilders
				.get("/test/resource")
				.accept(MediaType.APPLICATION_JSON)
				.header(HttpHeaders.AUTHORIZATION, "Basic " + this.base64EncodeClientCredentials());
		MockHttpServletResponse result = mockMvc.perform(requestBuilder).andReturn().getResponse();

		// then
		assertThat(result.getStatus()).isEqualTo(HttpStatus.OK.value());
		String requestId = result.getHeader("x-request-id");
		assertThat(requestId).isNotBlank();

		AuditLogRecord auditLogRecord = auditLogRecordRepository.findById(requestId);
		assertThat(auditLogRecord).isNotNull();
		assertThat(auditLogRecord.getClientKey()).isEqualTo(clientKey.getId());
	}

	private String base64EncodeClientCredentials() {
		String credentials = clientKey.getId() + ":" + clientKey.getSecretKey();
		return new String(Base64Utils.encode(credentials.getBytes()));
	}

	@Before
	public void setUp() {
		wireMockServer.start();
		configureFor("localhost", wireMockServer.port());

		clientKey = new ClientKey();
		clientKey.setApplicationType(ApplicationType.CONFIDENTIAL);
		clientKey.setId(UUID.randomUUID().toString());
		clientKey.setSecretKey(UUID.randomUUID().toString());

		ClientService service = new ClientService();
		service.setId("test");
		clientKey.setServices(Arrays.asList(service));

		when(clientKeyRepository.findById(clientKey.getId())).thenReturn(Optional.of(clientKey));
	}

	@After
	public void stopWireMockServer() {
		this.wireMockServer.stop();
	}

}
