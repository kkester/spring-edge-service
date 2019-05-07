package integration.steps;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import integration.core.ApiRequest;
import integration.core.ResponseResults;
import integration.core.RestApiFeature;
import io.pivotal.edge.keys.ApplicationType;
import io.pivotal.edge.keys.ClientKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
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

    @Given("^a typical \"([^\"]*)\" Client Key$")
    public void the_client_issues_GET_participant(String clientApiKeyType) {
        ApiRequest apiRequest = new ApiRequest();
        ClientKey clientKey = new ClientKey();
        clientKey.setApplicationType(ApplicationType.PUBLIC);
        clientKey.setId(publicApiKey);
        apiRequest.setClientKey(clientKey);
        restApiFeature.setCurrentRequest(apiRequest);
    }

    @When("^the client calls for \"([^\"]*)\" a (\\d+) times concurrently using \"([^\"]*)\"$")
    public void the_client_issues_GET_products(String resource, int count, String serviceId) throws InterruptedException {
        ApiRequest currentRequest = restApiFeature.getCurrentRequest();
        currentRequest.setUrl(String.format("/%s/%s", serviceId,resource));
        for (int x = 0; x <= count; x++) {
            restApiFeature.getResource(currentRequest);
        }
        while (restApiFeature.getResponses().size() < count) {
            log.info("Waiting for requests to finish ... {} out of {}", restApiFeature.getResponses().size(), count);
            TimeUnit.MILLISECONDS.sleep(100);
        }
    }

    @Then("^the client receives status codes of (\\d+) and (\\d+)$")
    public void the_client_receives_status_codes_of(int status1, int status2) {
        List<HttpStatus> expectedStatus = Arrays.asList(HttpStatus.resolve(status1), HttpStatus.resolve(status2));
        Set<HttpStatus> results = restApiFeature.getResponses().stream()
                .filter(r -> !expectedStatus.contains(r.getStatus()))
                .map(ResponseResults::getStatus)
                .collect(Collectors.toSet());
        assertThat(results).isEmpty();
    }

}
