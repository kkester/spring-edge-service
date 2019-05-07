package io.pivotal.edge.auditing;

import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

import java.net.InetAddress;
import java.net.UnknownHostException;

@Configuration
@EnableElasticsearchRepositories(basePackages = "io.pivotal.edge.auditing")
public class AuditingElasticsearchConfiguration {

    @Value("${elasticsearch.host}")
    private String esHost;

    @Value("${elasticsearch.port}")
    private int esPort;

    @Value("${elasticsearch.clustername}")
    private String clusterName;

    @Bean
    public Client client() throws UnknownHostException {
        Settings elasticsearchSettings = Settings.builder()
                .put("cluster.name", clusterName).build();
        TransportClient client = new PreBuiltTransportClient(elasticsearchSettings);
        TransportAddress transportAddress = new TransportAddress(InetAddress.getByName(esHost), esPort);
        client.addTransportAddress(transportAddress);
        return client;
    }

}
