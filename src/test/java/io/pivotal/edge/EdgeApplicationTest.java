package io.pivotal.edge;

import com.github.tomakehurst.wiremock.WireMockServer;
import io.pivotal.edge.auditing.AuditLogRecord;
import io.pivotal.edge.auditing.AuditLogRecordRepository;
import io.pivotal.edge.keys.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
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
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.util.Base64Utils;

import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
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

	@MockBean
	private AuditLogRecordRepository auditLogRecordRepository;

	@MockBean
	private ClientKeyRepository clientKeyRepository;

	private ClientKey confidentialClientKey;

	private ClientKey publicClientKey;

	@Before
	public void setUp() {
		wireMockServer.start();
		configureFor("localhost", wireMockServer.port());

		confidentialClientKey = new ClientKey();
		confidentialClientKey.setApplicationType(ApplicationType.CONFIDENTIAL);
		confidentialClientKey.setId(UUID.randomUUID().toString());
		confidentialClientKey.setSecretKey(UUID.randomUUID().toString());

		publicClientKey = new ClientKey();
		publicClientKey.setApplicationType(ApplicationType.PUBLIC);
		publicClientKey.setId(UUID.randomUUID().toString());
		publicClientKey.setSecretKey(UUID.randomUUID().toString());

		ClientService service = new ClientService();
		service.setId("test");
		publicClientKey.setServices(Arrays.asList(service));
		confidentialClientKey.setServices(Arrays.asList(service));

		when(clientKeyRepository.findById(publicClientKey.getId())).thenReturn(Optional.of(publicClientKey));
		when(clientKeyRepository.findById(confidentialClientKey.getId())).thenReturn(Optional.of(confidentialClientKey));
	}

	@Test
	public void shouldRouteRequestForPublicClientKey_WhenGivenValidCredentials() throws Exception {

		// given
		givenThat(get(urlEqualTo("/resource"))
				.willReturn(aResponse().withStatus(200)));

		// when
		RequestBuilder requestBuilder = MockMvcRequestBuilders
				.get("/test/resource?apiKey={0}", publicClientKey.getId())
				.accept(MediaType.APPLICATION_JSON);
		MockHttpServletResponse result = mockMvc.perform(requestBuilder)
				.andReturn()
				.getResponse();

		// then
		assertThat(result.getStatus()).isEqualTo(HttpStatus.OK.value());
		String requestId = result.getHeader("x-request-id");
		assertThat(requestId).isNotBlank();

		ArgumentCaptor<AuditLogRecord> auditLogRecordArgumentCaptor = ArgumentCaptor.forClass(AuditLogRecord.class);
		verify(auditLogRecordRepository).save(auditLogRecordArgumentCaptor.capture());
		AuditLogRecord auditLogRecord = auditLogRecordArgumentCaptor.getValue();
		assertThat(auditLogRecord.getClientKey()).isEqualTo(publicClientKey.getId());
	}

	@Test
	public void shouldRouteRequestForConfidentialClientKey_WhenGivenValidCredentials() throws Exception {

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

		ArgumentCaptor<AuditLogRecord> auditLogRecordArgumentCaptor = ArgumentCaptor.forClass(AuditLogRecord.class);
		verify(auditLogRecordRepository).save(auditLogRecordArgumentCaptor.capture());
		AuditLogRecord auditLogRecord = auditLogRecordArgumentCaptor.getValue();
		assertThat(auditLogRecord.getClientKey()).isEqualTo(confidentialClientKey.getId());
	}

	private String base64EncodeClientCredentials() {
		String credentials = confidentialClientKey.getId() + ":" + confidentialClientKey.getSecretKey();
		return new String(Base64Utils.encode(credentials.getBytes()));
	}

	@After
	public void stopWireMockServer() {
		this.wireMockServer.stop();
	}

}
