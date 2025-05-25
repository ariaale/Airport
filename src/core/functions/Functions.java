/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package core.functions;

import core.model.Flight;
import core.model.Location;
import core.model.Passenger;
import core.model.Plane;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 *
 * @author Alej
 */
public class Functions {

    public static int AgeCalculation(LocalDate birthdate) {
        return Period.between(birthdate, LocalDate.now()).getYears();
    }

    public static LocalDateTime ArrivalCalculation(Flight flight) {
        return flight.getDepartureDate().plusHours(flight.getHoursDurationScale()).plusHours(flight.getHoursDurationArrival()).plusMinutes(flight.getMinutesDurationScale()).plusMinutes(flight.getMinutesDurationArrival());
    }

    public static ArrayList<Flight> FlightOrderer(List<Flight> originalList) {
        ArrayList<Flight> orderedList = new ArrayList<>(originalList);
        Collections.sort(orderedList, Comparator.comparing(Flight::getDepartureDate));
        return orderedList;
    }

    public static ArrayList<Location> LocationOrderer(List<Location> originalList) {
        ArrayList<Location> orderedList = new ArrayList<>(originalList);
        Collections.sort(orderedList, Comparator.comparing(Location::getAirportId));
        return orderedList;
    }

    public static ArrayList<Passenger> PassengerOrderer(ArrayList<Passenger> originalList) {
        ArrayList<Passenger> orderedList = new ArrayList<>(originalList);
        Collections.sort(orderedList, Comparator.comparingLong(Passenger::getId));
        return orderedList;
    }

    public static ArrayList<Plane> PlaneOrderer(ArrayList<Plane> originalList) {
        ArrayList<Plane> orderedList = new ArrayList<>(originalList);
        Collections.sort(orderedList, Comparator.comparing(Plane::getId));
        return orderedList;
    }
    
}
