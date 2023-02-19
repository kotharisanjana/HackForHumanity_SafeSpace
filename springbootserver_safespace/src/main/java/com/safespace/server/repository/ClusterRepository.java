package com.safespace.server.repository;

import com.safespace.server.collection.Cluster;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClusterRepository extends MongoRepository<Cluster, String> {
}
