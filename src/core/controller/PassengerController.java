// core.controller.PassengerController.java
package core.controller;

import core.responses.Response;
import core.responses.Status;
import core.model.Flight;
import core.model.Passenger;
import core.storage.FlightStorage;
import core.storage.PassengerStorage;
import core.storage.LocationStorage; // Added for DataProcessor
import core.storage.PlaneStorage; // Added for DataProcessor
import core.storage.DataProcessor; // Changed import
import core.design.observer.UserManager;
import core.functions.PassengerManager;
import core.functions.Functions;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class PassengerController {

    private static final String DEFAULT_DAY_PLACEHOLDER = "Day";
    private static final String DEFAULT_MONTH_PLACEHOLDER = "Month";
    private static final String DEFAULT_YEAR_PLACEHOLDER = "Year";

    public static Response loadPassengersFromJson(String path) {
        try {
            // Instantiate DataProcessor with all required deposits
            DataProcessor dataLoader = new DataProcessor(
                FlightStorage.getInstance(),
                PlaneStorage.getInstance(),
                LocationStorage.getInstance(),
                PassengerStorage.getInstance()
            );
            dataLoader.loadPassengersFromFile(path); // Pass the path directly
            return new Response("Passengers loaded successfully! 👥", Status.OK);
        } catch (Exception e) {
            return new Response("Could not load passengers. Please try again later. 😔", Status.INTERNAL_SERVER_ERROR);
        }
    }

    public static Response getAllPassengers() {
        try {
            List<Passenger> passengers = Functions.PassengerOrderer(PassengerStorage.getInstance().getAll());
            return new Response("Passengers retrieved successfully! ✅", Status.OK, passengers);
        } catch (Exception e) {
            return new Response("Could not retrieve passengers. Please try again later. 😔", Status.INTERNAL_SERVER_ERROR, new ArrayList<>());
        }
    }

    public static Response getPassengersWithFormat() {
        try {
            List<Passenger> passengers = (List<Passenger>) getAllPassengers().getObject();
            ArrayList<String[]> data = passengers.stream().map(passenger -> {
                int passengerAge = Functions.AgeCalculation(passenger.getBirthDate());
                String fullPhone = "+" + passenger.getCountryPhoneCode() + " " + passenger.getPhone();
                return new String[]{
                    String.valueOf(passenger.getId()),
                    passenger.getFullname(),
                    passenger.getBirthDate().toString(),
                    String.valueOf(passengerAge),
                    fullPhone,
                    passenger.getCountry(),
                    String.valueOf(passenger.getNumFlights())
                };
            }).collect(Collectors.toCollection(ArrayList::new));
            return new Response("Passengers retrieved successfully! 📋", Status.OK, data);
        } catch (Exception e) {
            return new Response("Could not retrieve formatted passengers. Please try again later. 😔", Status.INTERNAL_SERVER_ERROR, new ArrayList<>());
        }
    }

    public static Response addPassenger(String id, String firstname, String lastname, String year, String month, String day, String countryPhoneCode, String phone, String country) {
        try {
            long longId = parsePassengerId(id);
            if (PassengerStorage.getInstance().get(id) != null) {
                throw new IllegalArgumentException("A passenger with this ID already exists. 🆔");
            }
            validateStringField(firstname, "The first name");
            validateStringField(lastname, "The last name");
            validateStringField(country, "The country");
            LocalDate birthDate = parseBirthDate(year, month, day);
            int intPhoneCode = parsePhoneCode(countryPhoneCode);
            long longPhone = parsePhoneNumber(phone);
            PassengerStorage.getInstance().add(new Passenger(longId, firstname, lastname, birthDate, intPhoneCode, longPhone, country));
            return new Response("Passenger created successfully! ✨", Status.CREATED);
        } catch (IllegalArgumentException e) {
            return new Response(e.getMessage(), Status.BAD_REQUEST);
        } catch (Exception e) {
            return new Response("An unexpected error occurred while adding the passenger. Please try again. 🚫", Status.INTERNAL_SERVER_ERROR);
        }
    }

    public static Response updatePassenger(String id, String firstname, String lastname, String year, String month, String day, String countryPhoneCode, String phone, String country) {
        try {
            long longId = parsePassengerId(id);
            Passenger passenger = getRequiredPassenger(id);
            validateStringField(firstname, "The first name");
            validateStringField(lastname, "The last name");
            validateStringField(country, "The country");
            LocalDate birthDate = parseBirthDate(year, month, day);
            int intPhoneCode = parsePhoneCode(countryPhoneCode);
            long longPhone = parsePhoneNumber(phone);

            Passenger updatedPassenger = new Passenger(longId, firstname, lastname, birthDate, intPhoneCode, longPhone, country);
            if (!PassengerStorage.getInstance().update(updatedPassenger)) {
                throw new IllegalStateException("Could not update passenger in the database. 💾");
            }
            return new Response("Passenger data updated successfully! ✏️", Status.OK);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return new Response(e.getMessage(), Status.BAD_REQUEST);
        } catch (Exception e) {
            return new Response("An unexpected error occurred while updating the passenger. Please try again. 🚫", Status.INTERNAL_SERVER_ERROR);
        }
    }

    public static Response addToFlight(String passengerId, String flightId) {
        try {
            Passenger passenger = getRequiredPassenger(passengerId);
            Flight flight = getRequiredFlight(flightId);

            if (flight.getNumPassengers() == flight.getPlane().getMaxCapacity()) {
                throw new IllegalArgumentException("The flight is full. No more passengers can be added. 🚫");
            }
            new PassengerManager().addPassenger(flight, passenger);
            return new Response("Passenger added to flight successfully! ➕✈️", Status.OK);
        } catch (IllegalArgumentException e) {
            return new Response(e.getMessage(), Status.BAD_REQUEST);
        } catch (Exception e) {
            return new Response("An unexpected error occurred while adding the passenger to the flight. Please try again. 🚫", Status.INTERNAL_SERVER_ERROR);
        }
    }

    public static Response showPassengerFlights(String passengerId) {
        try {
            Passenger passenger = getRequiredPassenger(passengerId);
            List<Flight> flights = passenger.getFlights();
            if (flights.isEmpty()) {
                return new Response("The passenger has no registered flights. 🙁", Status.OK, new ArrayList<>());
            }
            ArrayList<String[]> data = flights.stream().map(flight -> {
                LocalDateTime arrivalDate = Functions.ArrivalCalculation(flight);
                return new String[]{
                    flight.getId(),
                    flight.getDepartureDate().toString(),
                    arrivalDate.toString()
                };
            }).collect(Collectors.toCollection(ArrayList::new));
            return new Response("Passenger flights retrieved successfully! 📋", Status.OK, data);
        } catch (IllegalArgumentException e) {
            return new Response(e.getMessage(), Status.BAD_REQUEST);
        } catch (Exception e) {
            return new Response("Could not retrieve passenger flights. Please try again later. 😔", Status.INTERNAL_SERVER_ERROR, new ArrayList<>());
        }
    }

    public static Response changeUser(String id) {
        try {
            if (id.equals("Select User")) {
                throw new IllegalArgumentException("Please select a user first. 👥");
            }
            UserManager.getInstance().setCurrentUser(getRequiredPassenger(id));
            return new Response("User changed successfully! 🔄", Status.OK);
        } catch (IllegalArgumentException e) {
            return new Response(e.getMessage(), Status.BAD_REQUEST);
        } catch (Exception e) {
            return new Response("An unexpected error occurred while changing the user. Please try again. 🚫", Status.INTERNAL_SERVER_ERROR, new ArrayList<>());
        }
    }

    public static long parsePassengerId(String id) {
        if (id.isEmpty()) {
            throw new IllegalArgumentException("ID cannot be empty. 🆔🚫");
        }
        try {
            long longId = Long.parseLong(id);
            if (longId < 0) {
                throw new IllegalArgumentException("ID must be positive. 👍");
            }
            if (String.valueOf(longId).length() > 15) {
                throw new IllegalArgumentException("ID cannot exceed 15 digits. 🔢");
            }
            return longId;
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("ID must be numeric. 🔢");
        }
    }

    public static Passenger getRequiredPassenger(String passengerId) {
        Passenger passenger = PassengerStorage.getInstance().get(passengerId);
        if (passenger == null) {
            throw new IllegalArgumentException("Passenger with the selected ID not found. 🧑‍✈️🔍❌");
        }
        return passenger;
    }

    public static Flight getRequiredFlight(String flightId) {
        Flight flight = FlightStorage.getInstance().get(flightId);
        if (flight == null) {
            throw new IllegalArgumentException("Flight with the selected ID not found. ✈️🔍❌");
        }
        return flight;
    }

    public static void validateStringField(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " cannot be empty. 📝🚫");
        }
    }

    public static LocalDate parseBirthDate(String yearStr, String monthStr, String dayStr) {
        try {
            int year = parseYear(yearStr);
            int month = parseMonth(monthStr);
            int day = parseDay(dayStr);
            return LocalDate.of(year, month, day);
        } catch (DateTimeException e) {
            throw new IllegalArgumentException("Invalid date. Please check the values. 🎂🗓️❌");
        }
    }

    public static int parseYear(String yearStr) {
        if (yearStr.equals(DEFAULT_YEAR_PLACEHOLDER)) {
            throw new IllegalArgumentException("You must choose a year before continuing. 🗓️");
        }
        try {
            int year = Integer.parseInt(yearStr);
            if (year > LocalDate.now().getYear() || year < 1900) {
                throw new IllegalArgumentException("The year must be between 1900 and the current year. 🗓️");
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
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("The day must be a number. 🔢");
        }
    }

    public static int parsePhoneCode(String countryPhoneCode) {
        if (countryPhoneCode.isEmpty()) {
            throw new IllegalArgumentException("The phone country code cannot be empty. 📞🚫");
        }
        try {
            int code = Integer.parseInt(countryPhoneCode);
            if (code < 0) {
                throw new IllegalArgumentException("The phone code must be positive. 📞👍");
            }
            if (String.valueOf(code).length() > 3) {
                throw new IllegalArgumentException("The phone code cannot exceed 3 digits. 📞🔢");
            }
            return code;
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("The phone code must be numeric. 📞🔢");
        }
    }

    public static long parsePhoneNumber(String phone) {
        if (phone.isEmpty()) {
            throw new IllegalArgumentException("The phone number cannot be empty. 📱🚫");
        }
        try {
            long phoneNumber = Long.parseLong(phone);
            if (phoneNumber < 0) {
                throw new IllegalArgumentException("The phone number must be positive. 📱👍");
            }
            if (String.valueOf(phoneNumber).length() > 11) {
                throw new IllegalArgumentException("The phone number cannot exceed 11 digits. 📱🔢");
            }
            return phoneNumber;
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("The phone number must be numeric. 📱🔢");
        }
    }
}