package com.sethsaperstein.flinkcontrolapi.config;

import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KubernetesConfig {

    @Value("${kubernetes.api.server}")
    private String apiServer;

    @Value("${kubernetes.api.token}")
    private String apiToken;

    @Value("${kubernetes.api.ca-certificate}")
    private String caCertificate;

    @Bean
    public KubernetesClient kubernetesClient() {
        ConfigBuilder configBuilder = new ConfigBuilder();
        if (!apiServer.isEmpty()) {
            configBuilder.withMasterUrl(apiServer);
        }
        if (!apiToken.isEmpty()) {
            configBuilder.withOauthToken(apiToken);
        }
        if (!caCertificate.isEmpty()) {
            configBuilder.withCaCertData(caCertificate);
        }

        Config config = configBuilder.build();

        return new KubernetesClientBuilder()
            .withConfig(config)
            .build();
    }
}
