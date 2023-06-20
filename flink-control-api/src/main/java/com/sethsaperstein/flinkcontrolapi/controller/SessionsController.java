package com.sethsaperstein.flinkcontrolapi.controller;


import org.apache.flink.table.gateway.rest.message.session.OpenSessionRequestBody;
import org.apache.flink.table.gateway.rest.message.session.OpenSessionResponseBody;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/cluster/{cluster_name}/sessions")
public class SessionsController {

    private final RestTemplate restTemplate;

    @Autowired
    public SessionsController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }


    @PostMapping("")
    public ResponseEntity<OpenSessionResponseBody> openSession(
        @PathVariable("cluster_name") String clusterName,
        @RequestBody OpenSessionRequestBody openSessionRequestBody
    ) {
        String baseUrl = "http://localhost:8083";
        String url = baseUrl + "/cluster/" + clusterName + "/sessions";
        return restTemplate.postForEntity(url, openSessionRequestBody, OpenSessionResponseBody.class);
    }
}
