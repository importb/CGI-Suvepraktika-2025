package com.rainervana.flight_planner_backend.service;

import com.rainervana.flight_planner_backend.model.Flight;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FlightServiceImpl implements FlightService {
    private final List<Flight> flights = new ArrayList<>();

    @PostConstruct
    private void initSampleFlights() {  // test flights, for testing. todo: add more
        flights.add(new Flight(1L,
                "BT123",
                "TLL",
                "WAW",
                LocalDateTime.of(2025, 4, 1, 10, 30),
                LocalDateTime.of(2025, 4, 1, 11, 55),
                new BigDecimal("120.50"),
                "Boeing 737")
        );
    }

    @Override
    public List<Flight> findFlights(String destination, LocalDate date, Long maxDurationMinutes, BigDecimal maxPrice) {
        return flights.stream()
                .filter(flight -> destination == null || flight.getDestination().equalsIgnoreCase(destination))
                .filter(flight -> date == null || flight.getDepartureTime().toLocalDate().equals(date))
                .filter(flight -> maxDurationMinutes == null || flight.getDuration().toMinutes() <= maxDurationMinutes)
                .filter(flight -> maxPrice == null || flight.getPrice().compareTo(maxPrice) <= 0)
                .collect(Collectors.toList());
    }
}
