package com.rainervana.flight_planner_backend.controller;

import com.rainervana.flight_planner_backend.dto.BookingRequest;
import com.rainervana.flight_planner_backend.dto.BookingResponse;
import com.rainervana.flight_planner_backend.dto.SeatMapResponse;
import com.rainervana.flight_planner_backend.model.Flight;
import com.rainervana.flight_planner_backend.service.FlightService;
import com.rainervana.flight_planner_backend.service.SeatService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/flights")
public class FlightController {
    private static final Logger log = LoggerFactory.getLogger(FlightController.class);

    private final FlightService flightService;
    private final SeatService seatService;
    private long currentBookingId = 0L;

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
        log.info("Fetching all flights.");
        return flightService.findFlights(destination, date, maxDurationMinutes, maxPrice);
    }

    @GetMapping("/{flightId}")
    public Optional<Flight> getFlight(@PathVariable Long flightId) {
        log.info("Fetching single flight with an ID: {}.", flightId);
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
        log.info("Fetching seat map for flight with an ID: {}.", flightId);

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

    @PostMapping("/bookings")
    public ResponseEntity<BookingResponse> createBooking(@RequestBody BookingRequest bookingRequest) {
        log.info("Creating booking request.");
        log.info("Booking request: {}", bookingRequest);

        // Find the flight.
        Flight flight = flightService.findFlightById(bookingRequest.getFlightId()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Flight not found with ID: " + bookingRequest.getFlightId()));

        // Calculate the price.
        BigDecimal pricePerPassenger = flight.getPrice();

        if (pricePerPassenger == null || pricePerPassenger.compareTo(BigDecimal.ZERO) <= 0) {
            log.error("Booking failed: Invalid base price found for flight ID: {}", flight.getId());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Cannot process booking due to invalid flight price configuration.");
        }


        BigDecimal totalPrice = pricePerPassenger.multiply(BigDecimal.valueOf(bookingRequest.getPassengers()));
        log.debug("Base price calculated for {} passengers: {}", bookingRequest.getPassengers(), totalPrice);

        for (String seatNr : bookingRequest.getSelectedSeats()) {
            Optional<Boolean> isFirstClassOpt = seatService.isSeatFirstClass(flight.getId(), seatNr);

            if (isFirstClassOpt.isEmpty()) {
                log.warn("Booking failed: Invalid seat number {} provided for flight {}", seatNr, flight.getId());
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid seat number selected: " + seatNr);
            }

            if (isFirstClassOpt.get()) {
                totalPrice = totalPrice.add(BigDecimal.valueOf(50));
            }
        }

        log.info("Calculated price of the flight is {}", totalPrice);

        currentBookingId += 1;

        return ResponseEntity.ok(new BookingResponse(
                currentBookingId,
                flight.getId(),
                flight.getFlightNr(),
                bookingRequest.getPassengers(),
                bookingRequest.getSelectedSeats(),
                totalPrice,
                LocalDateTime.now()
        ));
    }
}
