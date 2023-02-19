package com.safespace.server.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.safespace.server.collection.Cluster;
import com.safespace.server.collection.Location;
import com.safespace.server.service.ClusterService;
import com.safespace.server.service.LocationService;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@RestController
@RequestMapping("/cluster")
public class ClusterController {

    @Autowired
    private ClusterService clusterService;
    @Autowired
    private LocationService locationService;

    private RestTemplate restTemplate;

    public ClusterController() {
        restTemplate = new RestTemplate();
        restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
    }

    @PostMapping
    public String save(@RequestBody Cluster cluster) {
        return clusterService.save(cluster);
    }

    @PostMapping("/saveAll")
    public String saveAll(@RequestBody List<Cluster> clusters){
        return clusterService.saveAll(clusters);
    }

    @GetMapping
    public List<Cluster> getAllCluster() {
        return clusterService.getAllCluster();
    }

    @DeleteMapping
    public String deleteAll() {
        return clusterService.deleteAll();
    }

    //@GetMapping("/saveCluster")
    //@Scheduled(fixedRate = 10000)
    public String saveClusterToDb() {
        final String URL = "http://172.20.197.227:5000/clusters";
        System.out.println("executed cron job");
        List<Location> locations = locationService.getAllLocation();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<List<Location>> request = new HttpEntity<>(locations, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(URL, request, String.class);
        String responseBody = response.getBody();
        System.out.println(responseBody);
        ObjectMapper objectMapper = new ObjectMapper();
        List<Cluster> clusters = null;

        try {
            clusters = objectMapper.readValue(responseBody, new TypeReference<List<Cluster>>() {});
            clusterService.deleteAll();
            clusterService.saveAll(clusters);
        } catch (Exception e) {
            // handle the exception
            e.printStackTrace();
        }
        System.out.println("successfully exuecuted");
        return "success";
    }
}
