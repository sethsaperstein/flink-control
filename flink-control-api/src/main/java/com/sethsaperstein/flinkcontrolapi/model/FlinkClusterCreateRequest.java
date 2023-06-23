package com.sethsaperstein.flinkcontrolapi.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FlinkClusterCreateRequest {
    private String clusterName;

    @JsonCreator
    public FlinkClusterCreateRequest(@JsonProperty("clusterName") String clusterName) {
        this.clusterName = clusterName;
    }
}
