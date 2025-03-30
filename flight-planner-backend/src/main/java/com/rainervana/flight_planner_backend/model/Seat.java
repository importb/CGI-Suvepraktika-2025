package com.rainervana.flight_planner_backend.model;

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
    private boolean isFirstClass;
}
