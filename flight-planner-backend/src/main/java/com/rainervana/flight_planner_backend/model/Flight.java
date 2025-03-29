package com.rainervana.flight_planner_backend.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.FetchType;
import jakarta.persistence.GenerationType;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@Entity
@Data
@NoArgsConstructor
public class Flight {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // flight information
    private String flightNr;
    private String origin;
    private String destination;
    private LocalDateTime departureTime;
    private LocalDateTime arrivalTime;
    private BigDecimal price;
    private String aircraftType;

    // store occupied seat numbers for this specific flight
    @ElementCollection(fetch = FetchType.EAGER)
    private List<String> occupiedSeatNrs = new ArrayList<>();


    public Flight(Long id, String flightNr, String origin, String destination, LocalDateTime departureTime, LocalDateTime arrivalTime, BigDecimal price, String aircraftType) {
        this.id = id;
        this.flightNr = flightNr;
        this.origin = origin;
        this.destination = destination;
        this.departureTime = departureTime;
        this.arrivalTime = arrivalTime;
        this.price = price;
        this.aircraftType = aircraftType;
    }

    /**
     * Calculates the duration between the arrival time and the departure time.
     *
     * @return  -   time elapsed between departure and arrival.
     */
    public Duration getDuration() {
        if (departureTime == null || arrivalTime == null) {
            return Duration.ZERO;
        }
        return Duration.between(departureTime, arrivalTime);
    }
}
