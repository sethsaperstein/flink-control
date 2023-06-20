package com.sethsaperstein.flinkcontrolapi.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FlinkCluster {
    private String clusterName;

    @JsonCreator
    public FlinkCluster(@JsonProperty("clusterName") String clusterName) {
        this.clusterName = clusterName;
    }
}
