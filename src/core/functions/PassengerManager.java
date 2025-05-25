package core.functions;

import core.model.Flight;
import core.model.Passenger;
import core.storage.FlightStorage;
import core.storage.PassengerStorage;

public class PassengerManager {

    public void addPassenger(Flight flight, Passenger passenger) {
        FlightStorage flightStorage = FlightStorage.getInstance();
        PassengerStorage passengerStorage = PassengerStorage.getInstance();

        flight.getPassengers().add(passenger);
        passenger.getFlights().add(flight);

        passengerStorage.update(passenger);
        flightStorage.update(flight);
    }
}