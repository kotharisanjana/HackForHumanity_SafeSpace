package com.safespace.server.collection;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@Builder
@Document(collection = "location")
public class Location {
    @Id
    private String uid;
    private Double latitude;
    private Double longitude;
    private Long radius;
}
