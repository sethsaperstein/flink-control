package com.sethsaperstein.flinkcontrolapi.controller;

import com.sethsaperstein.flinkcontrolapi.config.FlinkDeploymentClientManager;
import com.sethsaperstein.flinkcontrolapi.config.KubernetesClientManager;
import com.sethsaperstein.flinkcontrolapi.model.FlinkCluster;
import com.sethsaperstein.flinkcontrolapi.util.Utils;
import io.fabric8.kubernetes.api.model.StatusDetails;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientException;
import org.apache.flink.kubernetes.operator.api.lifecycle.ResourceLifecycleState;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class FlinkClusterControllerIntegrationTest {
    private static final Logger logger = LoggerFactory.getLogger(FlinkClusterControllerIntegrationTest.class);
    private static final String NAMESPACE = "flinktest";
    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    KubernetesClientManager kubernetesClientManager;

    @Autowired
    FlinkDeploymentClientManager flinkDeploymentClientManager;

    @AfterEach
    public void tearDown() throws InterruptedException {
        // wait for namespace to create before test tear down
        Thread.sleep(10000);

        KubernetesClient kubernetesClient = kubernetesClientManager.getClient();

        try {
            List<StatusDetails> statusList = kubernetesClient.namespaces().withName(NAMESPACE).delete();
            if (!statusList.isEmpty()) {

                for (StatusDetails statusDetails : statusList) {
                    if (!statusDetails.getCauses().isEmpty()) {
                        logger.warn("Error deleting {} {}: {}", statusDetails.getKind(), statusDetails.getName(), statusDetails.getCauses());
                    } else {
                        logger.info("Successfully deleted {} {}", statusDetails.getKind(), statusDetails.getName());
                    }
                }
            }
        } catch (KubernetesClientException e) {
            logger.warn("Error deleting namespace and its resources: {}", NAMESPACE, e);
        }
    }

    @Test
    public void testCreateSessionCluster() {
        String clusterName = "flinktest";
        FlinkCluster flinkClusterRequest = FlinkCluster.builder().clusterName(clusterName).build();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Create a request entity with the desired cluster name
        HttpEntity<FlinkCluster> requestEntity = new HttpEntity<>(flinkClusterRequest, headers);

        // Perform the HTTP POST request
        ResponseEntity<FlinkCluster> responseEntity = restTemplate.exchange(
            "http://localhost:" + port + "/cluster/session",
            HttpMethod.POST,
            requestEntity,
            FlinkCluster.class
        );

        // Verify the response
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        FlinkCluster flinkCluster = responseEntity.getBody();
        assert flinkCluster != null;
        assertEquals(clusterName, flinkCluster.getClusterName());

        assertDoesNotThrow(() -> Utils.waitForFlinkDeploymentStatus(
            clusterName,
            NAMESPACE,
            Duration.ofSeconds(10),
            List.of(ResourceLifecycleState.DEPLOYED, ResourceLifecycleState.STABLE),
            flinkDeploymentClientManager.getFlinkDeploymentClient()
        ));
    }
}