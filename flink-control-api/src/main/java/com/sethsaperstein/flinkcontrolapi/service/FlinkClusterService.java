package com.sethsaperstein.flinkcontrolapi.service;

import com.sethsaperstein.flinkcontrolapi.config.FlinkDeploymentClientManager;
import com.sethsaperstein.flinkcontrolapi.config.KubernetesClientManager;
import com.sethsaperstein.flinkcontrolapi.util.Utils;
import io.fabric8.kubernetes.api.model.*;
import io.fabric8.kubernetes.api.model.rbac.Role;
import io.fabric8.kubernetes.api.model.rbac.RoleBinding;
import io.fabric8.kubernetes.api.model.rbac.RoleBindingBuilder;
import io.fabric8.kubernetes.api.model.rbac.RoleBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.Resource;
import org.apache.flink.kubernetes.operator.api.FlinkDeployment;
import org.apache.flink.kubernetes.operator.api.spec.FlinkDeploymentSpec;
import org.apache.flink.kubernetes.operator.api.spec.FlinkVersion;
import org.apache.flink.kubernetes.operator.api.spec.JobManagerSpec;
import org.apache.flink.kubernetes.operator.api.spec.TaskManagerSpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

@Service
public class FlinkClusterService {
    private static final Long DELETE_TIMEOUT_MS = 10000L;
    private static final Logger logger = LoggerFactory.getLogger(FlinkClusterService.class);
    private final KubernetesClientManager kubernetesClientManager;
    private final FlinkDeploymentClientManager flinkDeploymentClientManager;
    private final SqlGatewayService sqlGatewayService;

    @Autowired
    public FlinkClusterService(
        KubernetesClientManager kubernetesClientManager,
        FlinkDeploymentClientManager flinkDeploymentClientManager,
        SqlGatewayService sqlGatewayService
    ) {
        this.kubernetesClientManager = kubernetesClientManager;
        this.flinkDeploymentClientManager = flinkDeploymentClientManager;
        this.sqlGatewayService = sqlGatewayService;
    }

    public void createFlinkSessionCluster(String name) {
        Namespace namespace = createNamespaceIfNotExists(name);
        ServiceAccount serviceAccount = createServiceAccountIfNotExists(name, namespace);
        Role role = createRoleIfNotExists(name, namespace);
        RoleBinding roleBinding = createRoleBindingIfNotExists(name, namespace, serviceAccount, role);
        FlinkDeployment flinkDeployment = createFlinkSessionClusterResourceIfNotExists(name, namespace, serviceAccount);
        sqlGatewayService.create(name, namespace, serviceAccount);
    }

    private RoleBinding createRoleBindingIfNotExists(
        String name,
        Namespace namespace,
        ServiceAccount serviceAccount,
        Role role
    ) {
        KubernetesClient client = kubernetesClientManager.getClient();
        RoleBinding existingRoleBinding = client
            .rbac()
            .roleBindings()
            .inNamespace(namespace.getMetadata().getName())
            .withName(name)
            .get();

        if (existingRoleBinding != null) {
            logger.info("Role binding already exists: {}", name);
            return existingRoleBinding;
        }

        logger.info("Creating role binding: {}", name);
        RoleBinding roleBinding = new RoleBindingBuilder()
            .withNewMetadata()
            .addToLabels("app.kubernetes.io/name", "flink-kubernetes-operator")
            .addToLabels("app.kubernetes.io/version", "1.0.1")
            .withName(name)
            .withNamespace(namespace.getMetadata().getName())
            .endMetadata()
            .withNewRoleRef()
            .withApiGroup("rbac.authorization.k8s.io")
            .withKind("Role")
            .withName(role.getMetadata().getName())
            .endRoleRef()
            .addNewSubject()
            .withKind("ServiceAccount")
            .withName(serviceAccount.getMetadata().getName())
            .endSubject()
            .build();

        return client.rbac().roleBindings().resource(roleBinding).create();
    }

    private Role createRoleIfNotExists(String name, Namespace namespace) {
        KubernetesClient client = kubernetesClientManager.getClient();

        Role existingRole = client
            .rbac()
            .roles()
            .inNamespace(namespace.getMetadata().getName())
            .withName(name)
            .get();

        if (existingRole != null) {
            logger.info("Role already exists: {}", name);
            return existingRole;
        }

        logger.info("Creating role: {}", name);
        Role role = new RoleBuilder()
            .withNewMetadata()
            .withNamespace(namespace.getMetadata().getName())
            .withName(name)
            .addToLabels("app.kubernetes.io/name", "flink-kubernetes-operator")
            .addToLabels("app.kubernetes.io/version", "1.0.1")
            .endMetadata()
            .addNewRule()
            .addToApiGroups("")
            .addToResources("pods", "configmaps", "services")
            .addToVerbs("*")
            .endRule()
            .addNewRule()
            .addToApiGroups("apps")
            .addToResources("deployments")
            .addToVerbs("*")
            .endRule()
            .build();

        return client.rbac().roles().resource(role).create();
    }

    private ServiceAccount createServiceAccountIfNotExists(String name, Namespace namespace) {
        KubernetesClient kubernetesClient = kubernetesClientManager.getClient();
        ServiceAccount existingServiceAccount = kubernetesClient
            .serviceAccounts()
            .inNamespace(namespace.getMetadata().getName())
            .withName(name)
            .get();

        if (existingServiceAccount != null) {
            return existingServiceAccount;
        }

        logger.info("Creating service account: {}", name);
        ServiceAccount serviceAccount = new ServiceAccountBuilder()
            .withNewMetadata()
            .withName(name)
            .withNamespace(namespace.getMetadata().getName())
            .endMetadata()
            .build();

        return kubernetesClient.serviceAccounts().resource(serviceAccount).create();
    }

    private Namespace createNamespaceIfNotExists(String namespaceName) {
        KubernetesClient kubernetesClient = kubernetesClientManager.getClient();
        Resource<Namespace> namespaceResource = kubernetesClient.namespaces().withName(namespaceName);
        if (namespaceResource.get() != null) {
            logger.info("Namespace already exists: {}", namespaceName);
            return namespaceResource.get();
        }
        logger.info("Creating namespace: {}", namespaceName);
        return kubernetesClient.namespaces().create(new NamespaceBuilder()
            .withNewMetadata()
            .withName(namespaceName)
            .endMetadata()
            .build());
    }

    private FlinkDeployment createFlinkSessionClusterResourceIfNotExists(
        String clusterName,
        Namespace namespace,
        ServiceAccount serviceAccount
    ) {
        FlinkDeployment existingFlinkDeployment =
            flinkDeploymentClientManager
                .getFlinkDeploymentClient()
                .inNamespace(namespace.getMetadata().getName())
                .withName(clusterName)
                .get();

        if (existingFlinkDeployment != null) {
            logger.info("FlinkDeployment already exists: {}", clusterName);
            return existingFlinkDeployment;
        }

        logger.info("Creating FlinkDeployment: {}", clusterName);
        FlinkDeployment flinkDeployment = new FlinkDeployment();

        ObjectMeta objectMeta = new ObjectMeta();
        objectMeta.setNamespace(namespace.getMetadata().getName());
        objectMeta.setName(clusterName);
        flinkDeployment.setMetadata(objectMeta);

        FlinkDeploymentSpec spec = new FlinkDeploymentSpec();
        spec.setImage("flink:1.16");
        spec.setFlinkVersion(FlinkVersion.v1_16);
        spec.setServiceAccount(serviceAccount.getMetadata().getName());

        org.apache.flink.kubernetes.operator.api.spec.Resource resource =
            new org.apache.flink.kubernetes.operator.api.spec.Resource();
        resource.setMemory("2048m");
        resource.setCpu(1.0);

        JobManagerSpec jm = new JobManagerSpec();
        jm.setResource(resource);
        spec.setJobManager(jm);

        TaskManagerSpec tm = new TaskManagerSpec();
        tm.setResource(resource);
        spec.setTaskManager(tm);

        Map<String, String> flinkConfig = new HashMap<>();
        flinkConfig.put("taskmanager.numberOfTaskSlots", "2");
        spec.setFlinkConfiguration(flinkConfig);

        flinkDeployment.setSpec(spec);

        FlinkDeployment newFlinkDeployment = flinkDeploymentClientManager.getFlinkDeploymentClient()
            .resource(flinkDeployment)
            .create();
        logger.info("Successfully created flink session cluster: {}", clusterName);
        return newFlinkDeployment;
    }

    public void deleteFlinkSessionCluster(String name) throws TimeoutException {
        deleteFlinkDeployment(name, name);
        sqlGatewayService.delete(name, name);
    }

    private void deleteFlinkDeployment(String name, String namespace) throws TimeoutException {
        Resource<FlinkDeployment> resource = flinkDeploymentClientManager
            .getFlinkDeploymentClient()
            .inNamespace(namespace)
            .withName(name);

        if (resource.get() != null) {
            logger.info("Deleting FlinkDeployment: {}", name);
            resource.delete();
            Utils.waitForFlinkDeploymentTeardown(
                name,
                namespace,
                Duration.ofSeconds(20),
                flinkDeploymentClientManager.getFlinkDeploymentClient());
        }
    }
}
