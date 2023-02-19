package com.safespace.server.controller;

import com.safespace.server.collection.Location;
import com.safespace.server.service.LocationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/location")
public class LocationController {

    @Autowired
    private LocationService locationService;

    @PostMapping
    public String save(@RequestBody Location location) {
        return locationService.save(location);
    }
    @GetMapping
    public List<Location> getAllLocation() {
        return locationService.getAllLocation();
    }

    @PostMapping("/save")
    public String checkAndSave(@RequestBody Location location) {
        return locationService.checkAndSave(location);
    }

    @PostMapping("/saveAll")
    public String saveAllLocations(@RequestBody List<Location> locations) {
        return locationService.saveAllLocations(locations);
    }
}
