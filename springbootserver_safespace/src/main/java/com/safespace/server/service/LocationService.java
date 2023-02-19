package com.safespace.server.service;

import com.safespace.server.collection.Location;

import java.util.List;

public interface LocationService {
    String save(Location location);

    List<Location> getAllLocation();

    String checkAndSave(Location location);

    String saveAllLocations(List<Location> locations);
}
