package io.pivotal.edge;

import com.github.tomakehurst.wiremock.WireMockServer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

@Configuration
@Slf4j
public class ApplicationTestConfig {

    @Bean
    @Profile("wire")
    WireMockServer wireMockServer(@Value("${wiremock.dynamic.port}") Integer port) {
        return new WireMockServer(options().port(port));
    }

}
