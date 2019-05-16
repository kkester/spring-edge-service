package integration.steps;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import integration.core.ApiRequest;
import integration.core.ResponseResults;
import integration.core.RestApiFeature;
import io.pivotal.edge.keys.web.ApplicationType;
import io.pivotal.edge.keys.web.ClientKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ContextConfiguration
@ActiveProfiles("int")
@Slf4j
public class EdgeServiceSteps {

    @Autowired
    private RestApiFeature restApiFeature;

    @Value("${application.integration.public-api-key}")
    private String publicApiKey;

    @Value("${application.integration.confidential-api-key}")
    private String confidentialApiKey;

    @Value("${application.integration.confidential-secret-key}")
    private String confidentialSecretKey;

    @Given("^a typical \"([^\"]*)\" Client Key$")
    public void the_client_issues_GET_participant(String clientApiKeyType) {
        ApiRequest apiRequest = new ApiRequest();
        ClientKey clientKey = new ClientKey();
        ApplicationType applicationType = ApplicationType.valueOf(clientApiKeyType.toUpperCase());
        if (ApplicationType.PUBLIC.equals(applicationType)) {
            clientKey.setApplicationType(ApplicationType.PUBLIC);
            clientKey.setClientId(publicApiKey);
        } else if (ApplicationType.CONFIDENTIAL.equals(applicationType)) {
            clientKey.setApplicationType(ApplicationType.CONFIDENTIAL);
            clientKey.setClientId(confidentialApiKey);
            clientKey.setSecretKey(confidentialSecretKey);
        }
        apiRequest.setClientKey(clientKey);
        restApiFeature.setCurrentRequest(apiRequest);
    }

    @When("^the client calls for \"([^\"]*)\" a (\\d+) times concurrently using \"([^\"]*)\"$")
    public void the_client_issues_GET_for_a_resource_from_a_service(String resource, int count, String serviceId) throws InterruptedException {
        ApiRequest currentRequest = restApiFeature.getCurrentRequest();
        currentRequest.setUrl(String.format("/%s/%s", serviceId,resource));
        List<ResponseResults> responses = Collections.synchronizedList(new ArrayList<>());
        for (int x = 1; x <= count; x++) {
            restApiFeature.getResource(currentRequest, responses);
        }
        while (responses.size() < count) {
            log.info("Waiting for requests to finish ... {} out of {}", responses.size(), count);
            TimeUnit.MILLISECONDS.sleep(100);
        }
        restApiFeature.setResponses(responses);
    }

    @Then("^the client receives status codes of (\\d+) and (\\d+)$")
    public void the_client_receives_status_codes_of(int status1, int status2) {
        List<HttpStatus> expectedStatus = Arrays.asList(HttpStatus.resolve(status1), HttpStatus.resolve(status2));
        Set<HttpStatus> results = this.getHttpStatuses();
        assertThat(results).containsAll(expectedStatus);
    }

    @Then("^the client receives status codes of (\\d+) and (\\d+) and (\\d+)$")
    public void the_client_receives_status_codes_of(int status1, int status2, int status3) {
        List<HttpStatus> expectedStatus = Arrays.asList(HttpStatus.resolve(status1), HttpStatus.resolve(status2), HttpStatus.resolve(status3));
        Set<HttpStatus> results = this.getHttpStatuses();
        assertThat(results).containsAll(expectedStatus);
    }

    private Set<HttpStatus> getHttpStatuses() {
        Collection<ResponseResults> restApiFeatureResponses = Collections.unmodifiableCollection(restApiFeature.getResponses());
        return restApiFeatureResponses.stream()
                .map(ResponseResults::getStatus)
                .collect(Collectors.toSet());
    }

}
