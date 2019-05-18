package io.pivotal.edge;

import com.github.tomakehurst.wiremock.WireMockServer;
import io.pivotal.edge.auditing.AuditLogRecord;
import io.pivotal.edge.auditing.AuditLogRecordRepository;
import io.pivotal.edge.keys.domain.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static io.pivotal.edge.EdgeApplicationConstants.REQUEST_ID_HEADER_NAME;
import static io.pivotal.edge.security.SecurityUtil.base64EncodeClientCredentials;
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
	private ClientDetailsEntityRepository clientDetailsRepository;

	@MockBean
	private ClientDetailsServiceEntityRepository clientDetailsServiceEntityRepository;

	@Autowired
	@Mock
	private BCryptPasswordEncoder passwordEncoder;

	private ClientDetailsEntity confidentialClientKey;

	private String confidentialSecretKey;

	private ClientDetailsEntity publicClientKey;

	@Before
	public void setUp() {
		wireMockServer.start();
		configureFor("localhost", wireMockServer.port());

		confidentialClientKey = new ClientDetailsEntity();
		confidentialClientKey.setAuthorizedGrantTypes("client_credentials");
		confidentialClientKey.setClientId(UUID.randomUUID().toString());
		confidentialSecretKey = UUID.randomUUID().toString();
		confidentialClientKey.setClientSecret(passwordEncoder.encode(confidentialSecretKey));

		publicClientKey = new ClientDetailsEntity();
		publicClientKey.setAuthorizedGrantTypes("implicit");
		publicClientKey.setClientId(UUID.randomUUID().toString());
		publicClientKey.setClientSecret(UUID.randomUUID().toString());

		ClientDetailsServiceEntity service = new ClientDetailsServiceEntity();
		ClientDetailsServiceKey key = new ClientDetailsServiceKey();
		key.setServiceId("test");
		service.setKey(key);

		when(clientDetailsRepository.findById(publicClientKey.getClientId())).thenReturn(Optional.of(publicClientKey));
		when(clientDetailsRepository.findById(confidentialClientKey.getClientId())).thenReturn(Optional.of(confidentialClientKey));
		when(clientDetailsServiceEntityRepository.findAllByKeyClientId(publicClientKey.getClientId())).thenReturn(Arrays.asList(service));
		when(clientDetailsServiceEntityRepository.findAllByKeyClientId(confidentialClientKey.getClientId())).thenReturn(Arrays.asList(service));
	}

	@Test
	public void shouldRouteRequestForPublicClientKey_WhenGivenValidCredentials() throws Exception {

		// given
		givenThat(get(urlEqualTo("/resource"))
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())));

		// when
		RequestBuilder requestBuilder = MockMvcRequestBuilders
				.get("/test/resource?apiKey={0}", publicClientKey.getClientId())
				.accept(MediaType.APPLICATION_JSON);
		MockHttpServletResponse result = mockMvc.perform(requestBuilder)
				.andReturn()
				.getResponse();

		// then
		assertThat(result.getStatus()).isEqualTo(HttpStatus.OK.value());
		String requestId = result.getHeader(REQUEST_ID_HEADER_NAME);
		assertThat(requestId).isNotBlank();

		ArgumentCaptor<AuditLogRecord> auditLogRecordArgumentCaptor = ArgumentCaptor.forClass(AuditLogRecord.class);
		verify(auditLogRecordRepository).save(auditLogRecordArgumentCaptor.capture());
		AuditLogRecord auditLogRecord = auditLogRecordArgumentCaptor.getValue();
		assertThat(auditLogRecord.getClientKey()).isEqualTo(publicClientKey.getClientId());
	}

	@Test
	public void shouldHandleErrorForPublicClientKey_WhenOriginUnavailable() throws Exception {

		// given
		givenThat(get(urlEqualTo("/resource"))
				.willReturn(aResponse().withStatus(HttpStatus.SERVICE_UNAVAILABLE.value())));

		// when
		RequestBuilder requestBuilder = MockMvcRequestBuilders
				.get("/test/resource?apiKey={0}", publicClientKey.getClientId())
				.accept(MediaType.APPLICATION_JSON);
		MockHttpServletResponse result = mockMvc.perform(requestBuilder)
				.andReturn()
				.getResponse();

		// then
		assertThat(result.getStatus()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE.value());
		String requestId = result.getHeader(REQUEST_ID_HEADER_NAME);
		assertThat(requestId).isNotBlank();

		ArgumentCaptor<AuditLogRecord> auditLogRecordArgumentCaptor = ArgumentCaptor.forClass(AuditLogRecord.class);
		verify(auditLogRecordRepository).save(auditLogRecordArgumentCaptor.capture());
		AuditLogRecord auditLogRecord = auditLogRecordArgumentCaptor.getValue();
		assertThat(auditLogRecord.getClientKey()).isEqualTo(publicClientKey.getClientId());
	}

	@Test
	public void shouldRouteRequestForConfidentialClientKey_WhenGivenValidCredentials() throws Exception {

		// given
		givenThat(get(urlEqualTo("/resource"))
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())));

		// when
		RequestBuilder requestBuilder = MockMvcRequestBuilders
				.get("/test/resource")
				.accept(MediaType.APPLICATION_JSON)
				.header(HttpHeaders.AUTHORIZATION, "Basic " + base64EncodeClientCredentials(confidentialClientKey.getClientId(), confidentialSecretKey));
		MockHttpServletResponse result = mockMvc.perform(requestBuilder).andReturn().getResponse();

		// then
		assertThat(result.getStatus()).isEqualTo(HttpStatus.OK.value());
		String requestId = result.getHeader(REQUEST_ID_HEADER_NAME);
		assertThat(requestId).isNotBlank();

		ArgumentCaptor<AuditLogRecord> auditLogRecordArgumentCaptor = ArgumentCaptor.forClass(AuditLogRecord.class);
		verify(auditLogRecordRepository).save(auditLogRecordArgumentCaptor.capture());
		AuditLogRecord auditLogRecord = auditLogRecordArgumentCaptor.getValue();
		assertThat(auditLogRecord.getClientKey()).isEqualTo(confidentialClientKey.getClientId());
	}

	@After
	public void stopWireMockServer() {
		this.wireMockServer.stop();
	}

}
