package core.functions;

import core.model.Flight;

public class FlightCoordinator {

    public void delay(Flight flight, int hours, int minutes) {
        flight.setDepartureDate(flight.getDepartureDate().plusHours(hours).plusMinutes(minutes));
    }
}