// core.deposit.data.loader.DataProcessor.java
package core.storage;

import core.model.Flight;
import core.model.Location;
import core.model.Passenger;
import core.model.Plane;
import core.storage.FlightStorage;
import core.storage.LocationStorage;
import core.storage.PassengerStorage;
import core.storage.PlaneStorage;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class DataProcessor {

    private final FlightStorage flights;
    private final PlaneStorage planes;
    private final LocationStorage locations;
    private final PassengerStorage passengers;

    public DataProcessor(FlightStorage flights, PlaneStorage planes, LocationStorage locations, PassengerStorage passengers) {
        this.flights = flights;
        this.planes = planes;
        this.locations = locations;
        this.passengers = passengers;
    }

    // Moved from LineFileReader.java
    private String readFile(String path) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            StringBuilder sb = new StringBuilder();
            String linea;
            while ((linea = br.readLine()) != null) {
                sb.append(linea);
            }
            return sb.toString();
        }
    }

    public void loadFlightsFromFile(String path) throws IOException {
        String jsonStr = readFile(path); // Use the internal readFile method
        JSONArray jsonArray = new JSONArray(jsonStr);
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject flightJson = jsonArray.getJSONObject(i);

            try {
                String id = flightJson.getString("id");

                String planeId = flightJson.getString("plane");
                Plane plane = this.planes.get(planeId);
                if (plane == null) {
                    System.err.println("Warning: Plane " + planeId + " not found for flight " + id + " ✈️");
                    continue;
                }

                String departureLocationId = flightJson.getString("departureLocation");
                Location departureLocation = this.locations.get(departureLocationId);
                if (departureLocation == null) {
                    System.err.println("Warning: Departure location " + departureLocationId + " not found for flight " + id + " 📍");
                    continue;
                }

                String arrivalLocationId = flightJson.getString("arrivalLocation");
                Location arrivalLocation = this.locations.get(arrivalLocationId);
                if (arrivalLocation == null) {
                    System.err.println("Warning: Arrival location " + arrivalLocationId + " not found for flight " + id + " 🗺️");
                    continue;
                }

                LocalDateTime departureDate = LocalDateTime.parse(flightJson.getString("departureDate"));

                int hoursDurationArrival = flightJson.getInt("hoursDurationArrival");
                int minutesDurationArrival = flightJson.getInt("minutesDurationArrival");

                String scaleLocationId = flightJson.optString("scaleLocation", null);
                int hoursDurationScale = flightJson.optInt("hoursDurationScale", 0);
                int minutesDurationScale = flightJson.optInt("minutesDurationScale", 0);

                Flight newFlight;
                if (scaleLocationId == null || scaleLocationId.isEmpty() || flightJson.isNull("scaleLocation")) {
                    newFlight = new Flight(
                            id, plane, departureLocation, arrivalLocation,
                            departureDate, hoursDurationArrival, minutesDurationArrival
                    );
                } else {
                    Location scaleLocation = this.locations.get(scaleLocationId);
                    if (scaleLocation == null) {
                        System.err.println("Warning: Scale location " + scaleLocationId + " not found for flight " + id + " 🌍");
                        continue;
                    }
                    newFlight = new Flight(
                            id, plane, departureLocation, scaleLocation, arrivalLocation,
                            departureDate, hoursDurationArrival, minutesDurationArrival,
                            hoursDurationScale, minutesDurationScale
                    );
                }
                this.flights.add(newFlight);

            } catch (Exception e) {
                System.err.println("Error loading flight: " + flightJson.toString() + " - " + e.getMessage() + " ❌");
            }
        }
    }

    public void loadLocationsFromFile(String path) throws IOException {
        String jsonStr = readFile(path); // Use the internal readFile method
        JSONArray jsonArray = new JSONArray(jsonStr);
        for(int i = 0; i < jsonArray.length(); i++) {
            JSONObject locationJson = jsonArray.getJSONObject(i);

            try {
                String airportId = locationJson.getString("airportId");
                String airportName = locationJson.getString("airportName");
                String airportCity = locationJson.getString("airportCity");
                String airportCountry = locationJson.getString("airportCountry");

                double airportLatitude = locationJson.getDouble("airportLatitude");
                double airportLongitude = locationJson.getDouble("airportLongitude");

                Location newLocation = new Location(airportId,airportName,airportCity,airportCountry,airportLatitude,airportLongitude);
                this.locations.add(newLocation);
            } catch (Exception e) {
                System.err.println("Error loading location: " + locationJson.toString() + " - " + e.getMessage() + " ❌");
            }
        }
    }

    public void loadPassengersFromFile(String path) throws IOException {
        String jsonStr = readFile(path); // Use the internal readFile method
        JSONArray jsonArray = new JSONArray(jsonStr);
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject passengerJson = jsonArray.getJSONObject(i);

            try {
                long id = passengerJson.getLong("id");
                String firstname = passengerJson.getString("firstname");
                String lastname = passengerJson.getString("lastname");
                String birthDateString = passengerJson.getString("birthDate");
                LocalDate birthDate = LocalDate.parse(birthDateString);
                int countryPhoneCode = passengerJson.getInt("countryPhoneCode");
                long phone = passengerJson.getLong("phone");
                String country = passengerJson.getString("country");

                Passenger newPassenger = new Passenger(id,firstname,lastname,birthDate,countryPhoneCode,phone,country);
                this.passengers.add(newPassenger);
            } catch (Exception e) {
                System.err.println("Error loading passenger: " + passengerJson.toString() + " - " + e.getMessage() + " ❌");
            }
        }
    }

    public void loadPlanesFromFile(String path) throws IOException {
        String jsonStr = readFile(path); // Use the internal readFile method
        JSONArray jsonArray = new JSONArray(jsonStr);
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject planeJson = jsonArray.getJSONObject(i);

            try {
                String id = planeJson.getString("id");
                String brand = planeJson.getString("brand");
                String model = planeJson.getString("model");
                int maxCapacity = planeJson.getInt("maxCapacity");
                String airline = planeJson.getString("airline");

                Plane newPlane = new Plane(id,brand,model,maxCapacity,airline);
                this.planes.add(newPlane);
            } catch (Exception e) {
                System.err.println("Error loading plane: " + planeJson.toString() + " - " + e.getMessage() + " ❌");
            }
        }
    }
}