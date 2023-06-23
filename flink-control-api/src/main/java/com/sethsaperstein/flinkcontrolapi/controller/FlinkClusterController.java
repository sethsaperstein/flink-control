package com.sethsaperstein.flinkcontrolapi.controller;

import com.sethsaperstein.flinkcontrolapi.model.FlinkClusterCreateRequest;
import com.sethsaperstein.flinkcontrolapi.model.FlinkClusterCreateResponse;
import com.sethsaperstein.flinkcontrolapi.model.FlinkClusterDeleteRequest;
import com.sethsaperstein.flinkcontrolapi.model.FlinkClusterDeleteResponse;
import com.sethsaperstein.flinkcontrolapi.service.FlinkClusterService;
import com.sethsaperstein.flinkcontrolapi.util.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.concurrent.TimeoutException;


@RestController
@RequestMapping("/cluster")
public class FlinkClusterController {

    private final FlinkClusterService flinkClusterService;

    @Autowired
    public FlinkClusterController(FlinkClusterService flinkClusterService) {
        this.flinkClusterService = flinkClusterService;
    }

    @PostMapping("/session")
    public ResponseEntity<FlinkClusterCreateResponse> createSessionCluster(@RequestBody FlinkClusterCreateRequest request) {
        String name;
        if (request.getClusterName() != null) {
            name = request.getClusterName();
        } else {
            // TODO: check collision
            name = Utils.generateRandomName();
        }

        flinkClusterService.createFlinkSessionCluster(name);
        FlinkClusterCreateResponse response = FlinkClusterCreateResponse.builder().clusterName(name).build();
        response.setClusterName(name);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/session")
    public ResponseEntity<FlinkClusterDeleteResponse> deleteSessionCluster(@RequestBody @Valid FlinkClusterDeleteRequest request) throws TimeoutException {
        String name = request.getClusterName();
        flinkClusterService.deleteFlinkSessionCluster(name);
        FlinkClusterDeleteResponse response = FlinkClusterDeleteResponse.builder().clusterName(name).build();
        return ResponseEntity.ok(response);
    }
}
