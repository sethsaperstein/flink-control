package com.sethsaperstein.flinkcontrolapi.config;

import io.fabric8.kubernetes.api.model.KubernetesResourceList;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import org.apache.flink.kubernetes.operator.api.FlinkDeployment;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FlinkDeploymentClientManager {
    private final KubernetesClient kubernetesClient;

    public FlinkDeploymentClientManager(KubernetesClient kubernetesClient) {
        this.kubernetesClient = kubernetesClient;
    }

    public MixedOperation<FlinkDeployment, KubernetesResourceList<FlinkDeployment>, Resource<FlinkDeployment>> getFlinkDeploymentClient() {
        return kubernetesClient.resources(FlinkDeployment.class);
    }
}
