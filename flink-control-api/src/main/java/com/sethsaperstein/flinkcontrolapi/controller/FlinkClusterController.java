package com.sethsaperstein.flinkcontrolapi.controller;

import com.sethsaperstein.flinkcontrolapi.model.FlinkCluster;
import com.sethsaperstein.flinkcontrolapi.service.FlinkClusterService;
import com.sethsaperstein.flinkcontrolapi.util.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Locale;
import java.util.UUID;


@RestController
@RequestMapping("/cluster")
public class FlinkClusterController {

    private final FlinkClusterService flinkClusterService;

    @Autowired
    public FlinkClusterController(FlinkClusterService flinkClusterService) {
        this.flinkClusterService = flinkClusterService;
    }

    @PostMapping("/session")
    public ResponseEntity<FlinkCluster> createSessionCluster(@RequestBody FlinkCluster flinkClusterRequest) {
        String name;
        if (flinkClusterRequest.getClusterName() != null) {
            name = flinkClusterRequest.getClusterName();
        } else {
            // TODO: check collision
            name = Utils.generateRandomName();
        }

        flinkClusterService.createFlinkSessionCluster(name);
        FlinkCluster flinkClusterResponse = FlinkCluster.builder().clusterName(name).build();
        flinkClusterResponse.setClusterName(name);

        return ResponseEntity.ok(flinkClusterResponse);
    }
}
