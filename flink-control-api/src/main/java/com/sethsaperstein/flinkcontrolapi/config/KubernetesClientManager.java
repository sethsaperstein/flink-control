package com.sethsaperstein.flinkcontrolapi.config;

import io.fabric8.kubernetes.client.KubernetesClient;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;

@Component
public class KubernetesClientManager {
    @Getter
    public KubernetesClient client;

    @Autowired
    public KubernetesClientManager(KubernetesClient kubernetesClient) {
        this.client = kubernetesClient;
    }

    @PreDestroy
    public void closeClient() {
        client.close();
    }
}
