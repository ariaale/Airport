// core.controller.FlightController.java
package core.controller;

import core.responses.Response;
import core.responses.Status;
import core.model.Flight;
import core.model.Location;
import core.model.Plane;
import core.storage.FlightStorage;
import core.storage.LocationStorage;
import core.storage.PlaneStorage;
import core.storage.PassengerStorage; // Added for DataProcessor constructor
import core.storage.DataProcessor; // Changed import
import core.functions.FlightCoordinator;
import core.functions.Functions;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class FlightController {

    private static final String DEFAULT_TIME_PART_PLACEHOLDER = "Hour";
    private static final String DEFAULT_DAY_PLACEHOLDER = "Day";
    private static final String DEFAULT_MONTH_PLACEHOLDER = "Month";
    private static final String DEFAULT_YEAR_PLACEHOLDER = "Year";
    private static final String DEFAULT_LOCATION_PLACEHOLDER = "Location";

    public static Response loadFlightsFromJson(String path) {
        try {
            // Instantiate DataProcessor with all required deposits
            DataProcessor dataLoader = new DataProcessor(
                FlightStorage.getInstance(),
                PlaneStorage.getInstance(),
                LocationStorage.getInstance(),
                PassengerStorage.getInstance() // Include all deposits even if not directly used by this specific load method
            );
            dataLoader.loadFlightsFromFile(path); // Pass the path directly
            return new Response("Flights loaded successfully! ✈️", Status.OK);
        } catch (Exception e) {
            return new Response("Could not load flights. Please try again later. 😔", Status.INTERNAL_SERVER_ERROR);
        }
    }

    public static Response getAllFlights() {
        try {
            List<Flight> flights = Functions.FlightOrderer(FlightStorage.getInstance().getAll());
            return new Response("Flights retrieved successfully! ✅", Status.OK, flights);
        } catch (Exception e) {
            return new Response("Could not retrieve flights. Please try again later. 😔", Status.INTERNAL_SERVER_ERROR, new ArrayList<>());
        }
    }

    public static Response getFlightsWithFormat() {
        try {
            List<Flight> flights = (List<Flight>) getAllFlights().getObject();
            ArrayList<String[]> data = flights.stream().map(flight -> {
                LocalDateTime arrivalDate = Functions.ArrivalCalculation(flight);
                return new String[]{
                    flight.getId(),
                    flight.getDepartureLocation().getAirportId(),
                    flight.getArrivalLocation().getAirportId(),
                    flight.getScaleLocation() != null ? flight.getScaleLocation().getAirportId() : "-",
                    flight.getDepartureDate().toString(),
                    arrivalDate.toString(),
                    flight.getPlane().getId(),
                    String.valueOf(flight.getNumPassengers())
                };
            }).collect(Collectors.toCollection(ArrayList::new));
            return new Response("Flights retrieved successfully! 📋", Status.OK, data);
        } catch (Exception e) {
            return new Response("Could not retrieve formatted flights. Please try again later. 😔", Status.INTERNAL_SERVER_ERROR, new ArrayList<>());
        }
    }

    public static Response addFlight(String id, String planeId, String departureId, String arrivalId,
                                     String year, String month, String day, String hour, String minutes,
                                     String hoursArrivalStr, String minutesArrivalStr,
                                     String scaleId, String hoursScaleStr, String minutesScaleStr) {
        try {
            validateFlightId(id);
            if (FlightStorage.getInstance().get(id) != null) {
                throw new IllegalArgumentException("A flight with this ID already exists. 🆔");
            }

            Plane plane = getRequiredPlane(planeId);
            Location departure = getRequiredLocation(departureId, "departure");
            Location arrival = getRequiredLocation(arrivalId, "arrival");
            LocalDateTime departureLocalDate = parseDepartureDateTime(year, month, day, hour, minutes);
            int hoursArrival = parseTimePart(hoursArrivalStr, "arrival hour");
            int minutesArrival = parseTimePart(minutesArrivalStr, "arrival minute");

            if (hoursArrival == 0 && minutesArrival == 0) {
                throw new IllegalArgumentException("Flight duration must be greater than 00:00. ⏳");
            }

            Location scale = null;
            int hoursScale = 0;
            int minutesScale = 0;
            boolean hasScale = !scaleId.equals(DEFAULT_LOCATION_PLACEHOLDER);

            if (hasScale) {
                scale = getRequiredLocation(scaleId, "scale");
                hoursScale = parseTimePart(hoursScaleStr, "scale hour");
                minutesScale = parseTimePart(minutesScaleStr, "scale minute");
                if (hoursScale == 0 && minutesScale == 0) {
                    throw new IllegalArgumentException("Scale time must be greater than 0 if a scale is specified. ⏱️");
                }
            } else if (!hoursScaleStr.equals(DEFAULT_TIME_PART_PLACEHOLDER) || !minutesScaleStr.equals(DEFAULT_TIME_PART_PLACEHOLDER)) {
                throw new IllegalArgumentException("There cannot be scale time if there is no scale location. 📍");
            }

            Flight flight = hasScale
                    ? new Flight(id, plane, departure, scale, arrival, departureLocalDate, hoursArrival, minutesArrival, hoursScale, minutesScale)
                    : new Flight(id, plane, departure, arrival, departureLocalDate, hoursArrival, minutesArrival);

            FlightStorage.getInstance().add(flight);
            return new Response("Flight added successfully! ✨", Status.CREATED);
        } catch (IllegalArgumentException e) {
            return new Response(e.getMessage(), Status.BAD_REQUEST);
        } catch (Exception e) {
            return new Response("An unexpected error occurred while adding the flight. Please try again. 🚫", Status.INTERNAL_SERVER_ERROR);
        }
    }

    public static Response delayFlight(String flightId, String hour, String minutes) {
        try {
            Flight flight = getRequiredFlight(flightId);
            new FlightCoordinator().delay(flight, parseTimePart(hour, "hour"), parseTimePart(minutes, "minute"));
            return new Response("Flight delayed successfully! ⏰", Status.OK);
        } catch (IllegalArgumentException e) {
            return new Response(e.getMessage(), Status.BAD_REQUEST);
        } catch (Exception e) {
            return new Response("An unexpected error occurred while delaying the flight. Please try again. 🚫", Status.INTERNAL_SERVER_ERROR);
        }
    }

    public static void validateFlightId(String id) {
        if (id.length() != 6) {
            throw new IllegalArgumentException("Flight ID must have exactly 6 characters (3 letters followed by 3 numbers). 🔢");
        }
        if (!id.substring(0, 3).matches("[A-Z]{3}")) {
            throw new IllegalArgumentException("The first 3 characters of the ID must be uppercase letters. 🔠");
        }
        if (!id.substring(3, 6).matches("\\d{3}")) {
            throw new IllegalArgumentException("The last 3 characters of the ID must be numbers. 🔢");
        }
    }

    public static Plane getRequiredPlane(String planeId) {
        Plane plane = PlaneStorage.getInstance().get(planeId);
        if (plane == null) {
            throw new IllegalArgumentException("The plane does not exist. ✈️❌");
        }
        return plane;
    }

    public static Location getRequiredLocation(String locationId, String type) {
        Location location = LocationStorage.getInstance().get(locationId);
        if (location == null) {
            throw new IllegalArgumentException("The " + type + " location does not exist. 📍❌");
        }
        return location;
    }

    public static Flight getRequiredFlight(String flightId) {
        Flight flight = FlightStorage.getInstance().get(flightId);
        if (flight == null) {
            throw new IllegalArgumentException("The flight does not exist. ✈️🚫");
        }
        return flight;
    }

    public static LocalDateTime parseDepartureDateTime(String yearStr, String monthStr, String dayStr, String hourStr, String minutesStr) {
        int year = parseYear(yearStr);
        int month = parseMonth(monthStr);
        int day = parseDay(dayStr);
        int hour = parseTimePart(hourStr, "hour");
        int minutes = parseTimePart(minutesStr, "minute");

        try {
            LocalDateTime dateTime = LocalDateTime.of(year, month, day, hour, minutes);
            if (dateTime.isBefore(LocalDateTime.now())) {
                throw new IllegalArgumentException("The departure date cannot be in the past. 🕰️🔙");
            }
            return dateTime;
        } catch (DateTimeException e) {
            throw new IllegalArgumentException("The departure date is invalid or does not exist: " + e.getMessage() + " 📅🚫");
        }
    }

    public static int parseYear(String yearStr) {
        if (yearStr.equals(DEFAULT_YEAR_PLACEHOLDER)) {
            throw new IllegalArgumentException("You must choose a year before continuing. 🗓️");
        }
        try {
            int year = Integer.parseInt(yearStr);
            int currentYear = LocalDate.now().getYear();
            if (year < currentYear || year > currentYear + 5) {
                throw new IllegalArgumentException("Please enter a valid year between " + currentYear + " and " + (currentYear + 5) + ". 🗓️✔️");
            }
            return year;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("The year must be a number. 🔢");
        }
    }

    public static int parseMonth(String monthStr) {
        if (monthStr.equals(DEFAULT_MONTH_PLACEHOLDER)) {
            throw new IllegalArgumentException("You must choose a month before continuing. 🗓️");
        }
        try {
            int month = Integer.parseInt(monthStr);
            if (month < 1 || month > 12) {
                throw new IllegalArgumentException("The month must be between 1 and 12. 🗓️");
            }
            return month;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("The month must be a number. 🔢");
        }
    }

    public static int parseDay(String dayStr) {
        if (dayStr.equals(DEFAULT_DAY_PLACEHOLDER)) {
            throw new IllegalArgumentException("You must choose a day before continuing. 🗓️");
        }
        try {
            return Integer.parseInt(dayStr);
        }
        catch (NumberFormatException e) {
            throw new IllegalArgumentException("The day must be a number. 🔢");
        }
    }

    public static int parseTimePart(String timePartStr, String partName) {
        if (timePartStr.equals(DEFAULT_TIME_PART_PLACEHOLDER) || timePartStr.equals("Minute")) {
            throw new IllegalArgumentException("You must choose an " + partName + " before continuing. ⏰");
        }
        try {
            return Integer.parseInt(timePartStr);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("The " + partName + " must be a number. 🔢");
        }
    }
}