package integration;

import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;
import org.junit.runner.RunWith;
import org.springframework.test.context.ActiveProfiles;

@RunWith(Cucumber.class)
@CucumberOptions(features = "src/itest/resources/features",
        format = {"pretty", "html:build/reports/cucumber/html", "json:build/reports/cucumber/cucumber.json", "usage:build/reports/cucumber/usage.jsonx", "junit:build/reports/cucumber/junit.xml"})
@ActiveProfiles("int")
public class EdgeServiceApplicationIT {
}
