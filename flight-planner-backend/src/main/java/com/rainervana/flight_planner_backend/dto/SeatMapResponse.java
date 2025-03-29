package com.rainervana.flight_planner_backend.dto;

import com.rainervana.flight_planner_backend.model.Seat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SeatMapResponse {
    private int totalRows;
    private List<String> columns;
    private List<Seat> allSeats;
    private List<String> recommendedSeatNrs;
}