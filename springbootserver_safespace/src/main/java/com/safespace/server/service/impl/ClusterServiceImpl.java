package com.safespace.server.service.impl;

import com.safespace.server.collection.Cluster;
import com.safespace.server.repository.ClusterRepository;
import com.safespace.server.service.ClusterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ClusterServiceImpl implements ClusterService {

    @Autowired
    ClusterRepository clusterRepository;
    @Override
    public String save(Cluster cluster) {
        return clusterRepository.save(cluster).getClusterId();
    }

    @Override
    public List<Cluster> getAllCluster() {
        return clusterRepository.findAll();
    }

    @Override
    public String saveAll(List<Cluster> clusters) {
        clusterRepository.saveAll(clusters);
        return "successfully saved cluster info";
    }

    @Override
    public String deleteAll() {
        clusterRepository.deleteAll();
        return "successfully deleted all data";
    }
}
