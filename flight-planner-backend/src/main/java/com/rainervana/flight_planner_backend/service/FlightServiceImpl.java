package com.rainervana.flight_planner_backend.service;

import com.rainervana.flight_planner_backend.model.Flight;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Service
public class FlightServiceImpl implements FlightService {
    private final List<Flight> flights = new ArrayList<>();
    private final AtomicLong idCounter = new AtomicLong();

    private static final int LAYOUT_TOTAL_ROWS = 25;
    private static final List<String> LAYOUT_COLUMNS = List.of("A", "B", "C", "D", "E", "F");
    private static final double DEFAULT_OCCUPANCY_RATE = 0.4;

    @PostConstruct
    private void initSampleFlights() {  // generates random flights.
        LocalDateTime baseTime = LocalDateTime.of(2025, 4, 1, 10, 30);
        String[] destinations = {"WAW", "RIX", "HEL", "ARN", "OSL"};
        String[] origins = {"TLL", "RIX"};
        String[] aircraft = {"Boeing 737", "Airbus A320", "ATR 72"};

        for (int i = 0; i < 20; i++) {
            long currentId = idCounter.incrementAndGet();
            LocalDateTime departure = baseTime.plusDays(i % 5).plusHours(i % 3);
            LocalDateTime arrival = departure.plusMinutes(70 + (i * 3 % 55));
            BigDecimal price = new BigDecimal("95.00").add(BigDecimal.valueOf(i * 2.5));
            String flightNr = "FL" + (100 + i);
            String origin = origins[i % origins.length];
            String dest = destinations[i % destinations.length];
            String craft = aircraft[i % aircraft.length];

            // Create flight instance using the constructor
            Flight flight = new Flight(currentId, flightNr, origin, dest, departure, arrival, price, craft);

            // Generate and set occupied seats specifically for this flight
            List<String> occupiedSeats = generateRandomOccupiedSeatsForFlight(
            );
            flight.setOccupiedSeatNrs(occupiedSeats);

            flights.add(flight);
        }

        // Add the specific test flight with potentially different occupancy
        Flight specificFlight = new Flight(
                idCounter.incrementAndGet(), "BT123", "TLL", "WAW",
                LocalDateTime.of(2025, 5, 10, 10, 0),
                LocalDateTime.of(2025, 5, 10, 11, 30),
                new BigDecimal("150.00"), "Boeing 737"
        );
        specificFlight.setOccupiedSeatNrs(generateRandomOccupiedSeatsForFlight(
        ));
        flights.add(specificFlight);
    }

    /**
     * Helper method to generate a list of random seat numbers based on layout and rate.
     *
     * @return List<String> containing randomly chosen seat numbers.
     */
    private List<String> generateRandomOccupiedSeatsForFlight() {
        List<String> allPossibleSeatNrs = new ArrayList<>();
        for (int row = 1; row <= FlightServiceImpl.LAYOUT_TOTAL_ROWS; row++) {
            for (String col : FlightServiceImpl.LAYOUT_COLUMNS) {
                allPossibleSeatNrs.add(row + col);
            }
        }

        int numberToOccupy = (int) (allPossibleSeatNrs.size() * FlightServiceImpl.DEFAULT_OCCUPANCY_RATE);
        Collections.shuffle(allPossibleSeatNrs, ThreadLocalRandom.current());

        return new ArrayList<>(allPossibleSeatNrs.subList(0, Math.min(numberToOccupy, allPossibleSeatNrs.size())));
    }

    /**
     * Finds flights matching the specified criteria.
     *
     * @param destination           -   desired destination.
     * @param date                  -   desired departure date.
     * @param maxDurationMinutes    -   maximum flight duration in minutes.
     * @param maxPrice              -   maximum allowed price for the flight.
     * @return                      -   list of Flight objects matching the criteria.
     */
    @Override
    public List<Flight> findFlights(String destination, LocalDate date, Long maxDurationMinutes, BigDecimal maxPrice) {
        return flights.stream()
                .filter(flight -> destination == null || flight.getDestination().equalsIgnoreCase(destination))
                .filter(flight -> date == null || flight.getDepartureTime().toLocalDate().equals(date))
                .filter(flight -> maxDurationMinutes == null || flight.getDuration().toMinutes() <= maxDurationMinutes)
                .filter(flight -> maxPrice == null || flight.getPrice().compareTo(maxPrice) <= 0)
                .collect(Collectors.toList());
    }

    /**
     * Finds a specific flight by its id.
     *
     * @param id    -   flight id to find
     * @return      -   "Optional" containing the found Flight if found, otherwise empty "Optional"
     */
    @Override
    public Optional<Flight> findFlightById(Long id) {
        return flights.stream()
                .filter(flight -> flight.getId().equals(id))
                .findFirst();
    }
}
