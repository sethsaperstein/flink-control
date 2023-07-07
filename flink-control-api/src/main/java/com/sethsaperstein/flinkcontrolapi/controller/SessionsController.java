//package com.sethsaperstein.flinkcontrolapi.controller;
//
//
//import org.apache.flink.table.gateway.rest.message.operation.OperationStatusResponseBody;
//import org.apache.flink.table.gateway.rest.message.session.CloseSessionResponseBody;
//import org.apache.flink.table.gateway.rest.message.session.GetSessionConfigResponseBody;
//import org.apache.flink.table.gateway.rest.message.session.OpenSessionRequestBody;
//import org.apache.flink.table.gateway.rest.message.session.OpenSessionResponseBody;
//import org.apache.flink.table.gateway.rest.message.statement.ExecuteStatementRequestBody;
//import org.apache.flink.table.gateway.rest.message.statement.ExecuteStatementResponseBody;
//import org.apache.flink.table.gateway.rest.message.statement.FetchResultsResponseBody;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.*;
//import org.springframework.web.bind.annotation.*;
//import org.springframework.web.client.RestTemplate;
//
//import java.util.Objects;
//
//@RestController
//@RequestMapping("/sql/{cluster_name}/sessions")
//public class SessionsController {
//    private final static String SQL_GATEWAY_SERVICE_TEMPLATE = "http://%s-sql-gateway.%s.svc.cluster.local";
//    private final static String SQL_GATEWAY_SERVICE_PORT = "8080";
//    private final RestTemplate restTemplate;
//
//    @Autowired
//    public SessionsController(RestTemplate restTemplate) {
//        this.restTemplate = restTemplate;
//    }
//
//    @PostMapping("")
//    public ResponseEntity<OpenSessionResponseBody> openSession(
//        @PathVariable("cluster_name") String clusterName,
//        @RequestBody(required = false) OpenSessionRequestBody openSessionRequestBody
//    ) {
//        OpenSessionRequestBody request = Objects.requireNonNullElseGet(openSessionRequestBody,
//            () -> new OpenSessionRequestBody(null, null));
//
//        String baseUrl = String.format(SQL_GATEWAY_SERVICE_TEMPLATE, clusterName, clusterName) + ":" + SQL_GATEWAY_SERVICE_PORT;
//        String url = baseUrl + "/v1/sessions";
//        return restTemplate.postForEntity(url, request, OpenSessionResponseBody.class);
//    }
//
//    @DeleteMapping("")
//    public ResponseEntity<CloseSessionResponseBody> closeSession(
//        @PathVariable("cluster_name") String clusterName
//    ) {
//        String baseUrl = String.format(SQL_GATEWAY_SERVICE_TEMPLATE, clusterName, clusterName) + ":" + SQL_GATEWAY_SERVICE_PORT;
//        String url = baseUrl + "/v1/sessions";
//        HttpHeaders headers = new HttpHeaders();
//        headers.setContentType(MediaType.APPLICATION_JSON);
//        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);
//        return restTemplate.exchange(url, HttpMethod.DELETE, requestEntity, CloseSessionResponseBody.class);
//    }
//
//    @GetMapping("/{session_handle}")
//    public ResponseEntity<GetSessionConfigResponseBody> getSessionConfig(
//        @PathVariable("cluster_name") String clusterName,
//        @PathVariable("session_handle") String sessionHandle
//    ) {
//        String baseUrl = String.format(SQL_GATEWAY_SERVICE_TEMPLATE, clusterName, clusterName) + ":" + SQL_GATEWAY_SERVICE_PORT;
//        String url = baseUrl + "/v1/sessions/" + sessionHandle;
//        HttpHeaders headers = new HttpHeaders();
//        headers.setContentType(MediaType.APPLICATION_JSON);
//        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);
//        return restTemplate.exchange(url, HttpMethod.GET, requestEntity, GetSessionConfigResponseBody.class);
//    }
//
//    @PostMapping("/{session_handle}/operations/{operation_handle}/cancel")
//    public ResponseEntity<OperationStatusResponseBody> cancelOperation(
//        @PathVariable("cluster_name") String clusterName,
//        @PathVariable("session_handle") String sessionHandle,
//        @PathVariable("operation_handle") String operationHandle
//    ) {
//        String baseUrl = String.format(SQL_GATEWAY_SERVICE_TEMPLATE, clusterName, clusterName) + ":" + SQL_GATEWAY_SERVICE_PORT;
//        String url = baseUrl + "/v1/sessions/" + sessionHandle + "/operations/" + operationHandle + "/cancel";
//        HttpHeaders headers = new HttpHeaders();
//        headers.setContentType(MediaType.APPLICATION_JSON);
//        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);
//        return restTemplate.exchange(url, HttpMethod.POST, requestEntity, OperationStatusResponseBody.class);
//    }
//
//    @GetMapping("/{session_handle}/operations/{operation_handle}/result/{token}")
//    public ResponseEntity<FetchResultsResponseBody> fetchResults(
//        @PathVariable("cluster_name") String clusterName,
//        @PathVariable("session_handle") String sessionHandle,
//        @PathVariable("operation_handle") String operationHandle,
//        @PathVariable("token") String token
//    ) {
//        String baseUrl = String.format(SQL_GATEWAY_SERVICE_TEMPLATE, clusterName, clusterName) + ":" + SQL_GATEWAY_SERVICE_PORT;
//        String url = baseUrl + "/v1/sessions/" + sessionHandle + "/operations/" + operationHandle + "/result/" + token;
//        HttpHeaders headers = new HttpHeaders();
//        headers.setContentType(MediaType.APPLICATION_JSON);
//        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);
//        return restTemplate.exchange(url, HttpMethod.GET, requestEntity, FetchResultsResponseBody.class);
//    }
//
//    @PostMapping("/{session_handle}/statements")
//    public ResponseEntity<ExecuteStatementResponseBody> executeStatement(
//        @PathVariable("cluster_name") String clusterName,
//        @PathVariable("session_handle") String sessionHandle,
//        @RequestBody ExecuteStatementRequestBody executeStatementRequestBody
//    ) {
//        String baseUrl = String.format(SQL_GATEWAY_SERVICE_TEMPLATE, clusterName, clusterName) + ":" + SQL_GATEWAY_SERVICE_PORT;
//        String url = baseUrl + "/v1/sessions/" + sessionHandle + "/statements";
//        return restTemplate.postForEntity(url, executeStatementRequestBody, ExecuteStatementResponseBody.class);
//    }
//}
