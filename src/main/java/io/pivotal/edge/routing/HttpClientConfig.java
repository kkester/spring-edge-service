package io.pivotal.edge.routing;

import io.pivotal.edge.events.EventPublisher;
import org.springframework.cloud.commons.httpclient.ApacheHttpClientFactory;
import org.springframework.cloud.commons.httpclient.DefaultApacheHttpClientFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class HttpClientConfig {

    @Bean
    @Primary
    public ApacheHttpClientFactory httpClientFactory(EventPublisher eventPublisher) {
        return new DefaultApacheHttpClientFactory(EdgeServiceHttpClientBuilder.create(eventPublisher));
    }

}
