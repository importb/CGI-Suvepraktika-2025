package com.rainervana.flight_planner_backend.controller;

import com.rainervana.flight_planner_backend.dto.SeatMapResponse;
import com.rainervana.flight_planner_backend.model.Flight;
import com.rainervana.flight_planner_backend.service.FlightService;
import com.rainervana.flight_planner_backend.service.SeatService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/flights")
public class FlightController {
    private final FlightService flightService;
    private final SeatService seatService;

    public FlightController(FlightService flightService, SeatService seatService) {
        this.flightService = flightService;
        this.seatService = seatService;
    }

    @GetMapping
    public List<Flight> getFlights(
            @RequestParam(required = false) String destination,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) Long maxDurationMinutes,
            @RequestParam(required = false) BigDecimal maxPrice
    ) {
        return flightService.findFlights(destination, date, maxDurationMinutes, maxPrice);
    }

    @GetMapping("/{flightId}")
    public Optional<Flight> getFlight(@PathVariable Long flightId) {
        return flightService.findFlightById(flightId);
    }

    @GetMapping("/{flightId}/seats")
    public ResponseEntity<SeatMapResponse> getSeatMap(
            @PathVariable Long flightId,
            @RequestParam(defaultValue = "1") int numberOfPassengers,
            @RequestParam(required = false) Boolean preferWindow,
            @RequestParam(required = false) Boolean preferExtraLegroom,
            @RequestParam(required = false) Boolean preferNearExit
    ) {
        // find the flight.
        Flight flight = flightService.findFlightById(flightId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Flight not found with ID: " + flightId));

        // get seat map and if possible recommendations.
        SeatMapResponse seatMapResponse = seatService.getSeatMapAndRecommendations(
                flight,
                numberOfPassengers,
                Optional.ofNullable(preferWindow),
                Optional.ofNullable(preferExtraLegroom),
                Optional.ofNullable(preferNearExit)
        );

        return ResponseEntity.ok(seatMapResponse);
    }
}
