package com.rainervana.flight_planner_backend.model;

import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Seat {
    // Seat info
    private String seatNr;
    private int row;
    private String column;
    private boolean isWindow;
    private boolean hasExtraLegroom;
    private boolean isNearExit;
    private boolean isOccupied = false;

    @Column(nullable = false, name = "is_first_class")
    private boolean isFirstClass;
}
