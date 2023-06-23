package com.sethsaperstein.flinkcontrolapi.service;

import com.sethsaperstein.flinkcontrolapi.config.FlinkDeploymentClientManager;
import com.sethsaperstein.flinkcontrolapi.config.KubernetesClientManager;
import io.fabric8.kubernetes.api.model.*;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class SqlGatewayService {
    private static final Logger logger = LoggerFactory.getLogger(FlinkClusterService.class);
    private static final String SQL_GATEWAY_SUFFIX = "-sql-gateway";
    private final KubernetesClientManager kubernetesClientManager;
    private final FlinkDeploymentClientManager flinkDeploymentClientManager;

    @Autowired
    public SqlGatewayService(
        KubernetesClientManager kubernetesClientManager,
        FlinkDeploymentClientManager flinkDeploymentClientManager
    ) {
        this.kubernetesClientManager = kubernetesClientManager;
        this.flinkDeploymentClientManager = flinkDeploymentClientManager;
    }

    public void create(String name, Namespace namespace, ServiceAccount serviceAccount) {
        ConfigMap configMap = createConfigMapIfNotExists(name, namespace);
        Pod pod = createPodIfNotExists(name, namespace, serviceAccount, configMap);
        io.fabric8.kubernetes.api.model.Service service = createServiceIfNotExists(name, pod, namespace);
    }

    private ConfigMap createConfigMapIfNotExists(String name, Namespace namespace) {
        KubernetesClient kubernetesClient = kubernetesClientManager.getClient();;
        String configMapName = name + SQL_GATEWAY_SUFFIX;
        ConfigMap existingConfigMap = kubernetesClient
            .configMaps()
            .inNamespace(namespace.getMetadata().getName())
            .withName(configMapName)
            .get();

        if (existingConfigMap != null) {
            logger.info("ConfigMap already exists: {}", configMapName);
            return existingConfigMap;
        }

        logger.info("Creating ConfigMap: {}", configMapName);
        String jobManagerRpcAddress = name + "." + namespace.getMetadata().getName();
        String flinkConfig = "jobmanager.rpc.address: " + jobManagerRpcAddress;
        ConfigMap configMap = new ConfigMapBuilder()
            .withNewMetadata()
            .withName(configMapName)
            .withNamespace(namespace.getMetadata().getName())
            .endMetadata()
            .addToData("flink-conf.yaml", flinkConfig)
            .build();

        return kubernetesClient.configMaps().resource(configMap).create();
    }
    private Pod createPodIfNotExists(
        String name,
        Namespace namespace,
        ServiceAccount serviceAccount,
        ConfigMap configMap
    ) {
        KubernetesClient kubernetesClient = kubernetesClientManager.getClient();
        String podName = name + SQL_GATEWAY_SUFFIX;
        Pod existingPod = kubernetesClient
            .pods()
            .inNamespace(namespace.getMetadata().getName())
            .withName(podName)
            .get();

        if (existingPod != null) {
            logger.info("Pod already exists: {}", podName);
            return existingPod;
        }

        logger.info("Creating pod: {}", podName);
        String jobManagerRpcAddress = name + "." + namespace.getMetadata().getName();
        Map<String, String> labels = new HashMap<>();
        labels.put("app", name);
        Pod pod = new PodBuilder()
            .withNewMetadata()
                .withName(podName)
                .withNamespace(namespace.getMetadata().getName())
                .withLabels(labels)
            .endMetadata()
            .withNewSpec()
                .withServiceAccount(serviceAccount.getMetadata().getName())
                .addNewContainer()
                    .withName("sql-gateway")
                    .withImage("flink:1.16")
                    .withCommand(
                        "/opt/flink/bin/sql-gateway.sh",
                        "start-foreground",
                        "-Dsql-gateway.endpoint.rest.address=localhost")
                    .addNewEnv()
                        .withName("JOB_MANAGER_RPC_ADDRESS")
                        .withValue(jobManagerRpcAddress)
                    .endEnv()
                    .addNewVolumeMount()
                        .withName("config-volume")
                        .withMountPath("/opt/flink/conf")
                    .endVolumeMount()
                .endContainer()
                .addNewVolume()
                    .withName("config-volume")
                    .withNewConfigMap()
                        .withName(configMap.getMetadata().getName())
                    .endConfigMap()
                .endVolume()
                .endSpec()
            .build();

        return kubernetesClient.pods().resource(pod).create();
    }

    private io.fabric8.kubernetes.api.model.Service createServiceIfNotExists(
        String name,
        Pod pod,
        Namespace namespace
    ) {
        String serviceName = name + SQL_GATEWAY_SUFFIX;
        io.fabric8.kubernetes.api.model.Service existingService =
            kubernetesClientManager
                .getClient()
                .services()
                .inNamespace(namespace.getMetadata().getName())
                .withName(serviceName)
                .get();

        if (existingService != null) {
            logger.info("Service already exists: {}", serviceName);
            return existingService;
        }

        logger.info("Creating Service: {}", serviceName);

        ServicePort servicePort = new ServicePortBuilder()
            .withProtocol("TCP")
            .withPort(8080)
            .withTargetPort(new IntOrString(8083))
            .build();

        String appLabel = pod.getMetadata().getLabels().get("app");
        io.fabric8.kubernetes.api.model.Service service = new ServiceBuilder()
            .withNewMetadata()
            .withName(serviceName)
            .withNamespace(namespace.getMetadata().getName())
            .endMetadata()
            .withNewSpec()
            .withSelector(Map.of("app", appLabel))
            .withPorts(servicePort)
            .endSpec()
            .build();

        return kubernetesClientManager.getClient().services().create(service);
    }

    public void delete(String name, String namespace) {
        deletePod(name, namespace);
        deleteConfigMap(name, namespace);
    }

    private void deleteConfigMap(String name, String namespace) {
        KubernetesClient kubernetesClient = kubernetesClientManager.getClient();;
        String configMapName = name + SQL_GATEWAY_SUFFIX;
        Resource<ConfigMap> resource = kubernetesClient
            .configMaps()
            .inNamespace(namespace)
            .withName(configMapName);

        if (resource.get() != null) {
            logger.info("Deleting ConfigMap: {}", name);
            resource.delete();
        }
    }

    private void deletePod(String name, String namespace) {
        KubernetesClient kubernetesClient = kubernetesClientManager.getClient();
        String podName = name + SQL_GATEWAY_SUFFIX;
        Resource<Pod> resource = kubernetesClient
            .pods()
            .inNamespace(namespace)
            .withName(podName);

        if (resource.get() != null) {
            logger.info("Deleting Pod: {}", name);
            resource.delete();
        }
    }

}

