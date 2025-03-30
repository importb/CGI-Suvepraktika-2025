package com.rainervana.flight_planner_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingResponse {
    private Long bookingId;
    private Long flightId;
    private String flightNumber;
    private int passengers;
    private List<String> confirmedSeats;
    private BigDecimal totalPrice;
    private LocalDateTime bookingTime;
}
