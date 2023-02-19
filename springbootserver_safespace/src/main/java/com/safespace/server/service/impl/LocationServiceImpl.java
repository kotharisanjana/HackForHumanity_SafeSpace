package com.safespace.server.service.impl;

import com.safespace.server.collection.Location;
import com.safespace.server.repository.LocationRepository;
import com.safespace.server.service.LocationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LocationServiceImpl implements LocationService {

    @Autowired
    private LocationRepository locationRepository;
    @Override
    public String save(Location location) {
        return locationRepository.save(location).getUid();
    }

    @Override
    public List<Location> getAllLocation() {
        return locationRepository.findAll();
    }

    @Override
    public String checkAndSave(Location location) {
        // if location is present in the db, we need to check if person is moving too fast
        // by comparing the existing and new values
        if (locationRepository.existsById(location.getUid())) {
            Location currentLocation = locationRepository.findByUid(location.getUid());

            // if new distance is far remove from database
            if(isAwayThanCurrent(currentLocation.getLatitude(), currentLocation.getLongitude(), location.getLatitude(), location.getLongitude())) {
                locationRepository.deleteById(currentLocation.getUid());
                return "New point is very far from exiting lat long";
            }
            // else calculate the mean and save it in database
            else {
                Double meanLat = (currentLocation.getLatitude() + location.getLatitude())/2;
                Double meanLon = (currentLocation.getLongitude() + location.getLongitude())/2;
                currentLocation.setLatitude(meanLat);
                currentLocation.setLongitude(meanLon);
                locationRepository.save(currentLocation);
                return "successfully updated the value with mean value";
            }
        }
        // if id not present save it in the database
        else {
            return locationRepository.save(location).getUid();
        }

    }

    @Override
    public String saveAllLocations(List<Location> locations) {
        locationRepository.saveAll(locations);
        return "Successfully saved all the locations";
    }

    static boolean isAwayThanCurrent(Double currentLat, Double currentLon, Double newLat, Double newLon) {
        final double R = 6371000;
        double latDistance = Math.toRadians(newLat - currentLat);
        double lonDistance = Math.toRadians(newLon - currentLon);

        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(currentLat)) * Math.cos(Math.toRadians(newLat))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        double distance = R * c;

        return distance > 100; // distance is in meters
    }
}
