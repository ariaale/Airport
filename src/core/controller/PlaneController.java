// core.controller.PlaneController.java
package core.controller;

import core.responses.Response;
import core.responses.Status;
import core.model.Plane;
import core.storage.PlaneStorage;
import core.storage.FlightStorage; // Added for DataProcessor
import core.storage.LocationStorage; // Added for DataProcessor
import core.storage.PassengerStorage; // Added for DataProcessor
import core.storage.DataProcessor; // Changed import
import core.functions.Functions;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class PlaneController {

    public static Response loadPlanesFromJson(String path) {
        try {
            // Instantiate DataProcessor with all required deposits
            DataProcessor dataLoader = new DataProcessor(
                FlightStorage.getInstance(),
                PlaneStorage.getInstance(),
                LocationStorage.getInstance(),
                PassengerStorage.getInstance()
            );
            dataLoader.loadPlanesFromFile(path); // Pass the path directly
            return new Response("Planes loaded successfully! ✈️", Status.OK);
        } catch (Exception e) {
            return new Response("Could not load planes. Please try again later. 😔", Status.INTERNAL_SERVER_ERROR);
        }
    }

    public static Response getAllPlanes() {
        try {
            List<Plane> planes = Functions.PlaneOrderer(PlaneStorage.getInstance().getAll());
            return new Response("Planes retrieved successfully! ✅", Status.OK, planes);
        } catch (Exception e) {
            return new Response("Could not retrieve planes. Please try again later. 😔", Status.INTERNAL_SERVER_ERROR, new ArrayList<>());
        }
    }

    public static Response getPlanesWithFormat() {
        try {
            List<Plane> planes = (List<Plane>) getAllPlanes().getObject();
            ArrayList<String[]> data = planes.stream().map(plane -> new String[]{
                plane.getId(),
                plane.getBrand(),
                plane.getModel(),
                String.valueOf(plane.getMaxCapacity()),
                plane.getAirline()
            }).collect(Collectors.toCollection(ArrayList::new));
            return new Response("Planes retrieved successfully! 📋", Status.OK, data);
        } catch (Exception e) {
            return new Response("Could not retrieve formatted planes. Please try again later. 😔", Status.INTERNAL_SERVER_ERROR, new ArrayList<>());
        }
    }

    public static Response addPlane(String id, String brand, String model, String maxCapacityStr, String airline) {
        try {
            validatePlaneId(id);
            if (PlaneStorage.getInstance().get(id) != null) {
                throw new IllegalArgumentException("A plane with this ID already exists. 🆔");
            }
            validateStringField(brand, "The brand");
            validateStringField(model, "The model");
            validateStringField(airline, "The airline");
            PlaneStorage.getInstance().add(new Plane(id, brand, model, parseMaxCapacity(maxCapacityStr), airline));
            return new Response("Plane added successfully! ✨", Status.CREATED);
        } catch (IllegalArgumentException e) {
            return new Response(e.getMessage(), Status.BAD_REQUEST);
        } catch (Exception e) {
            return new Response("An unexpected error occurred while adding the plane. Please try again. 🚫", Status.INTERNAL_SERVER_ERROR);
        }
    }

    public static void validatePlaneId(String id) {
        if (id.isEmpty()) {
            throw new IllegalArgumentException("ID cannot be empty. 🆔🚫");
        }
        if (id.length() != 7) {
            throw new IllegalArgumentException("Invalid ID: must have exactly 7 characters (2 letters followed by 5 numbers). 🔢");
        }

        String idLetters = id.substring(0, 2);
        String idNumbers = id.substring(2, 7);

        if (!idLetters.matches("[A-Z]{2}")) {
            throw new IllegalArgumentException("Invalid ID: the first 2 characters must be uppercase letters. 🔠");
        }
        if (!idNumbers.matches("\\d{5}")) {
            throw new IllegalArgumentException("Invalid ID: the last 5 characters must be numbers. 🔢");
        }
    }

    public static void validateStringField(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " cannot be empty. 📝🚫");
        }
    }

    public static int parseMaxCapacity(String maxCapacityStr) {
        if (maxCapacityStr.isEmpty()) {
            throw new IllegalArgumentException("Max capacity cannot be empty. 🚫");
        }
        try {
            int capacity = Integer.parseInt(maxCapacityStr);
            if (capacity <= 0) {
                throw new IllegalArgumentException("Max capacity must be a positive number. 🔢👍");
            }
            return capacity;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Max capacity must be a number. 🔢");
        }
    }
}