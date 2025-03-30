package com.rainervana.flight_planner_backend.service;

import com.rainervana.flight_planner_backend.dto.SeatMapResponse;
import com.rainervana.flight_planner_backend.model.Flight;

import java.util.Optional;

public interface SeatService {
    SeatMapResponse getSeatMapAndRecommendations(
            Flight flight,
            int numberOfPassengers,
            Optional<Boolean> preferWindow,
            Optional<Boolean> preferExtraLegroom,
            Optional<Boolean> preferNearExit
    );

    Optional<Boolean> isSeatFirstClass(Long flightId, String seatNr);
}