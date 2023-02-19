package com.safespace.server.collection;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.jackson.Jacksonized;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Jacksonized
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "cluster")
public class Cluster {
    @Id
    @JsonProperty("clusterId")
    private String clusterId;

    @JsonProperty("centroidLat")
    private Double centroidLat;

    @JsonProperty("centroidLong")
    private Double centroidLong;

    @JsonProperty("personCount")
    private Integer personCount;

    @JsonProperty("radius")
    private Double radius;
}
