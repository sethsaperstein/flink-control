package com.sethsaperstein.flinkcontrolapi.controller;

import com.sethsaperstein.flinkcontrolapi.TestConfiguration;
import com.sethsaperstein.flinkcontrolapi.config.FlinkDeploymentClientManager;
import com.sethsaperstein.flinkcontrolapi.config.KubernetesClientManager;
import com.sethsaperstein.flinkcontrolapi.model.FlinkClusterCreateRequest;
import com.sethsaperstein.flinkcontrolapi.model.FlinkClusterCreateResponse;
import com.sethsaperstein.flinkcontrolapi.model.FlinkClusterDeleteRequest;
import com.sethsaperstein.flinkcontrolapi.model.FlinkClusterDeleteResponse;
import com.sethsaperstein.flinkcontrolapi.util.Utils;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.Resource;
import org.apache.flink.kubernetes.operator.api.FlinkDeployment;
import org.apache.flink.kubernetes.operator.api.lifecycle.ResourceLifecycleState;
import org.apache.flink.table.data.RowData;
import org.apache.flink.table.gateway.rest.message.session.OpenSessionRequestBody;
import org.apache.flink.table.gateway.rest.message.session.OpenSessionResponseBody;
import org.apache.flink.table.gateway.rest.message.statement.ExecuteStatementRequestBody;
import org.apache.flink.table.gateway.rest.message.statement.ExecuteStatementResponseBody;
import org.apache.flink.table.gateway.rest.message.statement.FetchResultsResponseBody;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.*;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
//@RunWith(SpringRunner.class)
//@SpringBootTest
@ContextConfiguration(classes = TestConfiguration.class)
public class FlinkClusterCreateRequestControllerIT {
    private static final Logger logger = LoggerFactory.getLogger(FlinkClusterCreateRequestControllerIT.class);
    private static final String NAMESPACE = "integ";
//    @LocalServerPort
//    private int port;

//    @Value("${app.port}")
    private final int port = 8888;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    KubernetesClientManager kubernetesClientManager;

    @Autowired
    FlinkDeploymentClientManager flinkDeploymentClientManager;

    @AfterEach
    public void tearDown() {
        KubernetesClient kubernetesClient = kubernetesClientManager.getClient();
        try {
            List<String> flinkDeploymentNames = flinkDeploymentClientManager
                .getFlinkDeploymentClient()
                .inNamespace(NAMESPACE)
                .list()
                .getItems()
                .stream()
                .map(x->x.getMetadata().getName())
                .toList();

            // wait for deletion of CRD's to avoid operator errors from the namespace entering a
            // terminating state and stuck resources due to finalizers
            for (String flinkDeploymentName: flinkDeploymentNames) {
                Resource<FlinkDeployment> flinkDeployment = flinkDeploymentClientManager
                    .getFlinkDeploymentClient()
                    .inNamespace(NAMESPACE)
                    .withName(flinkDeploymentName);

                // custom wait since .wait() cannot work in @AfterEach thread
                Utils.waitForFlinkDeploymentTeardown(
                    flinkDeploymentName,
                    NAMESPACE,
                    Duration.ofSeconds(100),
                    flinkDeploymentClientManager.getFlinkDeploymentClient()
                );

            }

            // print server logs
            Resource<Pod> pod = kubernetesClient
                .pods()
                .inNamespace(NAMESPACE)
                .withName("flink-control-api");

            if (pod.get() != null) {
                String logs = kubernetesClient.pods().inNamespace(NAMESPACE).withName("flink-control-api").getLog();
                logger.info("---------- API Logs ----------");
                logger.info(logs);
            }
//
//            List<StatusDetails> statusList = kubernetesClient
//                .namespaces()
//                .withName(NAMESPACE)
//                .delete();
//
//            if (!statusList.isEmpty()) {
//
//                for (StatusDetails statusDetails : statusList) {
//                    if (!statusDetails.getCauses().isEmpty()) {
//                        logger.warn("Error deleting {} {}: {}", statusDetails.getKind(), statusDetails.getName(), statusDetails.getCauses());
//                    } else {
//                        logger.info("Successfully deleted {} {}", statusDetails.getKind(), statusDetails.getName());
//                    }
//                }
//            }
        } catch (Throwable e) {
            logger.warn("Error deleting namespace and its resources: {}", NAMESPACE, e);
        }
    }

    @Test
    public void testCreateSessionCluster() {
        String clusterName = "integ";
        FlinkClusterCreateRequest flinkClusterCreateRequestRequest = FlinkClusterCreateRequest.builder().clusterName(clusterName).build();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Create a request entity with the desired cluster name
        HttpEntity<FlinkClusterCreateRequest> requestEntity = new HttpEntity<>(flinkClusterCreateRequestRequest, headers);

        // Create cluster
        ResponseEntity<FlinkClusterCreateResponse> responseEntity = restTemplate.exchange(
            "http://localhost:" + port + "/cluster/session",
            HttpMethod.POST,
            requestEntity,
            FlinkClusterCreateResponse.class
        );

        // Verify the response
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        FlinkClusterCreateResponse response = responseEntity.getBody();
        assert response != null;
        assertEquals(clusterName, response.getClusterName());

        assertDoesNotThrow(() -> Utils.waitForFlinkDeploymentStatus(
            clusterName,
            NAMESPACE,
            Duration.ofSeconds(100),
            List.of(ResourceLifecycleState.DEPLOYED, ResourceLifecycleState.STABLE),
            flinkDeploymentClientManager.getFlinkDeploymentClient()
        ));

        // create session in session cluster
        OpenSessionRequestBody openSessionRequestBody = new OpenSessionRequestBody(null, null);
        ResponseEntity<OpenSessionResponseBody> createSessionResponse = restTemplate.postForEntity(
            "http://localhost:" + port + "/sql/" + clusterName + "/v1/sessions",
            openSessionRequestBody,
            OpenSessionResponseBody.class);
        assertEquals(HttpStatus.OK, createSessionResponse.getStatusCode());
        assertNotNull(createSessionResponse.getBody());
        String sessionHandle = createSessionResponse.getBody().getSessionHandle();

        // Execute select 1 statement
        String statement = "SELECT 1";
        ExecuteStatementRequestBody executeStatementRequestBody = new ExecuteStatementRequestBody(statement);
        ResponseEntity<ExecuteStatementResponseBody> executeStatementResponse = restTemplate.postForEntity(
            "http://localhost" + port + "/sql/" + clusterName + "/v1/sessions/" + sessionHandle + "/statements/",
            executeStatementRequestBody,
            ExecuteStatementResponseBody.class);
        assertEquals(HttpStatus.OK, executeStatementResponse.getStatusCode());
        assertNotNull(executeStatementResponse.getBody());
        String operationHandle = executeStatementResponse.getBody().getOperationHandle();

        // Get operation result
        ResponseEntity<FetchResultsResponseBody> fetchResultsResponse = restTemplate.getForEntity(
            "http://localhost" + port + "/sql/" + clusterName + "/v1/sessions/" + sessionHandle
            + "/operations/" + operationHandle + "/result/" + "0",
            FetchResultsResponseBody.class
        );
        assertEquals(HttpStatus.OK, fetchResultsResponse.getStatusCode());
        assertNotNull(fetchResultsResponse.getBody());
        List<RowData> rowData = fetchResultsResponse.getBody().getResults().getData();
        assertNotNull(rowData);

        // Delete the cluster
        FlinkClusterDeleteRequest deleteRequest = FlinkClusterDeleteRequest.builder().clusterName(clusterName).build();
        headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Create a request entity with the desired cluster name
        HttpEntity<FlinkClusterDeleteRequest> deleteRequestEntity = new HttpEntity<>(deleteRequest, headers);

        // Perform the HTTP POST request
        ResponseEntity<FlinkClusterDeleteResponse> deleteResponseEntity = restTemplate.exchange(
            "http://localhost:" + port + "/cluster/session",
            HttpMethod.DELETE,
            deleteRequestEntity,
            FlinkClusterDeleteResponse.class
        );

        assertEquals(HttpStatus.OK, deleteResponseEntity.getStatusCode());
    }
}