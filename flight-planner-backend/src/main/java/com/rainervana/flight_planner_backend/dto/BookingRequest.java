package com.rainervana.flight_planner_backend.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingRequest {

    @NotNull(message = "Flight ID cannot be null")
    private Long flightId;

    @Min(value = 1, message = "Number of passengers must be at least 1")
    private int passengers;

    @NotEmpty(message = "At least one seat must be selected")
    private List<String> selectedSeats;
}
