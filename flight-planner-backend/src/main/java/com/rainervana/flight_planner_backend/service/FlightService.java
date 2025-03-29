package com.rainervana.flight_planner_backend.service;

import com.rainervana.flight_planner_backend.model.Flight;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface FlightService {
    List<Flight> findFlights(String destination, LocalDate date, Long maxDurationMinutes, BigDecimal maxPrice);
}