// core.controller.LocationController.java
package core.controller;

import core.responses.Response;
import core.responses.Status;
import core.model.Location;
import core.storage.LocationStorage;
import core.storage.PlaneStorage;
import core.storage.FlightStorage;
import core.storage.PassengerStorage;
import core.storage.DataProcessor; // Changed import
import core.functions.Functions;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class LocationController {

    public static Response loadLocationsFromJson(String path) {
        try {
            // Instantiate DataProcessor with all required deposits
            DataProcessor dataLoader = new DataProcessor(
                FlightStorage.getInstance(),
                PlaneStorage.getInstance(),
                LocationStorage.getInstance(),
                PassengerStorage.getInstance()
            );
            dataLoader.loadLocationsFromFile(path); // Pass the path directly
            return new Response("Locations loaded successfully! 🗺️", Status.OK);
        } catch (Exception e) {
            return new Response("Could not load locations. Please try again later. 😔", Status.INTERNAL_SERVER_ERROR);
        }
    }

    public static Response getAllLocations() {
        try {
            List<Location> locations = Functions.LocationOrderer(LocationStorage.getInstance().getAll());
            return new Response("Locations retrieved successfully! ✅", Status.OK, locations);
        } catch (Exception e) {
            return new Response("Could not retrieve locations. Please try again later. 😔", Status.INTERNAL_SERVER_ERROR, new ArrayList<>());
        }
    }

    public static Response getLocationsWithFormat() {
        try {
            List<Location> locations = (List<Location>) getAllLocations().getObject();
            ArrayList<String[]> data = locations.stream().map(location -> new String[]{
                location.getAirportId(),
                location.getAirportName(),
                location.getAirportCity(),
                location.getAirportCountry()
            }).collect(Collectors.toCollection(ArrayList::new));
            return new Response("Locations retrieved successfully! 📋", Status.OK, data);
        } catch (Exception e) {
            return new Response("Could not retrieve formatted locations. Please try again later. 😔", Status.INTERNAL_SERVER_ERROR, new ArrayList<>());
        }
    }

    public static Response addLocation(String id, String name, String city, String country, String longitudeStr, String latitudeStr) {
        try {
            validateLocationId(id);
            if (LocationStorage.getInstance().get(id) != null) {
                throw new IllegalArgumentException("A location with this ID already exists. 🆔");
            }
            validateStringField(name, "The name");
            validateStringField(city, "The city");
            validateStringField(country, "The country");
            LocationStorage.getInstance().add(new Location(id, name, city, country,
                    parseCoordinate(longitudeStr, "longitude", -180, 180),
                    parseCoordinate(latitudeStr, "latitude", -90, 90)));
            return new Response("Location added successfully! ✨", Status.CREATED);
        } catch (IllegalArgumentException e) {
            return new Response(e.getMessage(), Status.BAD_REQUEST);
        } catch (Exception e) {
            return new Response("An unexpected error occurred while adding the location. Please try again. 🚫", Status.INTERNAL_SERVER_ERROR);
        }
    }

    public static void validateLocationId(String id) {
        if (id == null || id.length() != 3) {
            throw new IllegalArgumentException("The ID must have exactly 3 characters. 🆔");
        }
        if (!id.matches("[A-Z]{3}")) {
            throw new IllegalArgumentException("The ID can only contain uppercase letters. 🔠");
        }
    }

    public static void validateStringField(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " cannot be empty. 📝🚫");
        }
    }

    public static double parseCoordinate(String coordStr, String coordName, double min, double max) {
        try {
            double coord = Double.parseDouble(coordStr);
            if (coord < min || coord > max) {
                throw new IllegalArgumentException("The " + coordName + " must be between " + min + " and " + max + ". 📏");
            }
            return coord;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("The " + coordName + " must be a valid number. 🔢✔️");
        }
    }
}