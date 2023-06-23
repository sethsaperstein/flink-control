package com.sethsaperstein.flinkcontrolapi.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
@Builder
public class FlinkClusterCreateResponse {
    @NotNull(message = "clusterName must not be null")
    private String clusterName;

    @JsonCreator
    public FlinkClusterCreateResponse(@JsonProperty("clusterName") String clusterName) {
        this.clusterName = clusterName;
    }
}
