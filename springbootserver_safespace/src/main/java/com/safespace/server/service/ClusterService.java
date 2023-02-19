package com.safespace.server.service;

import com.safespace.server.collection.Cluster;

import java.util.List;

public interface ClusterService {
    String save(Cluster cluster);

    List<Cluster> getAllCluster();

    String saveAll(List<Cluster> clusters);

    String deleteAll();
}
