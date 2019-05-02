package io.pivotal.edge.routing;

import io.pivotal.edge.events.EventPublisher;
import org.springframework.cloud.commons.httpclient.ApacheHttpClientFactory;
import org.springframework.cloud.commons.httpclient.DefaultApacheHttpClientFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.event.ApplicationEventMulticaster;
import org.springframework.context.event.SimpleApplicationEventMulticaster;
import org.springframework.core.task.SimpleAsyncTaskExecutor;

@Configuration
public class HttpClientConfig {

    @Bean
    @Primary
    public ApacheHttpClientFactory httpClientFactory(EventPublisher eventPublisher) {
        return new DefaultApacheHttpClientFactory(GatewayHttpClientBuilder.create(eventPublisher));
    }

}
