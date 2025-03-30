package com.rainervana.flight_planner_backend.service;

import com.rainervana.flight_planner_backend.dto.SeatMapResponse;
import com.rainervana.flight_planner_backend.model.Flight;
import com.rainervana.flight_planner_backend.model.Seat;

import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;


@Service
public class SeatServiceImpl implements SeatService {
    public static final int DEFAULT_TOTAL_ROWS = 25;
    public static final List<String> DEFAULT_COLUMNS = List.of("A", "B", "C", "D", "E", "F");
    private static final List<Integer> EXIT_ROWS = List.of(1, 12, 24);
    private static final List<Integer> EXTRA_LEG_ROWS = List.of(10, 11, 12);
    private static final List<String> WINDOW_COLUMNS = List.of("A", "F");

    @Override
    public SeatMapResponse getSeatMapAndRecommendations(
            Flight flight,
            int numberOfPassengers,
            Optional<Boolean> preferWindow,
            Optional<Boolean> preferExtraLegroom,
            Optional<Boolean> preferNearExit
    ) {
        if (numberOfPassengers <= 0) {
            numberOfPassengers = 1;
        }

        // get the pre-defined occupied seats and generate the full layout
        Set<String> occupiedForThisFlight = new HashSet<>(flight.getOccupiedSeatNrs());
        List<Seat> allSeats = generateSeatLayout(
                occupiedForThisFlight
        );

        // filter available seats
        List<Seat> availableSeats = allSeats.stream()
                .filter(seat -> !seat.isOccupied())
                .collect(Collectors.toList());

        // find recommendations based on available seats.
        List<Seat> recommendedSeats = findRecommendedSeats(
                availableSeats,
                numberOfPassengers,
                preferWindow.orElse(false),
                preferExtraLegroom.orElse(false),
                preferNearExit.orElse(false)
        );

        List<String> recommendedSeatNrs = recommendedSeats.stream().map(Seat::getSeatNr).collect(Collectors.toList());

        // return the response DTO
        return new SeatMapResponse(DEFAULT_TOTAL_ROWS, DEFAULT_COLUMNS, allSeats, recommendedSeatNrs);
    }

    /**
     * Generates the full seat layout for a plane, marking seats as occupied
     * based on the provided set of occupied seat numbers.
     */
    private List<Seat> generateSeatLayout(Set<String> occupiedSeatNrs) {
        List<Seat> seats = new ArrayList<>();
        for (int row = 1; row <= SeatServiceImpl.DEFAULT_TOTAL_ROWS; row++) {
            for (String col : SeatServiceImpl.DEFAULT_COLUMNS) {
                String seatNr = row + col;

                boolean isWindow = WINDOW_COLUMNS.contains(col);
                boolean hasExtraLegroom = EXTRA_LEG_ROWS.contains(row);
                boolean isNearExit = EXIT_ROWS.contains(row);
                boolean isOccupied = occupiedSeatNrs.contains(seatNr);
                boolean isFirstClass = (row == 1 || row == 2);

                seats.add(new Seat(seatNr, row, col, isWindow, hasExtraLegroom, isNearExit, isOccupied, isFirstClass));
            }
        }

        // sort for consistent order
        seats.sort(Comparator.comparingInt(Seat::getRow).thenComparing(Seat::getColumn));

        return seats;
    }

    /**
     * Finds a recommended group of seats based on availability, number of passengers, and preferences.
     *
     * @param availableSeats        -   list of all currently available seats on the flight.
     * @param numberOfPassengers    -   number of seats required for the group.
     * @param preferWindow          -   true / false based on is window seats are preferred.
     * @param preferExtraLegroom    -   true / false based on if extra legroom is preferred.
     * @param preferNearExit        -   true / false based on if near exit seats are preffered.
     * @return                      -   a list containing recommended seats.
     */
    private List<Seat> findRecommendedSeats(
            List<Seat> availableSeats,
            int numberOfPassengers,
            boolean preferWindow,
            boolean preferExtraLegroom,
            boolean preferNearExit
    ) {
        // filter by preferences.
        List<Seat> potentialSeats = availableSeats.stream()
                .filter(seat -> !preferWindow || seat.isWindow())
                .filter(seat -> !preferExtraLegroom || seat.isHasExtraLegroom())
                .filter(seat -> !preferNearExit || seat.isNearExit())
                .toList();

        if (potentialSeats.isEmpty() && availableSeats.isEmpty()) {
            return Collections.emptyList();
        } else if (potentialSeats.isEmpty()) {
            return Collections.emptyList();
        }

        // group by row and search for consecutive blocks.
        Map<Integer, List<Seat>> seatsByRow = potentialSeats.stream()
                .sorted(Comparator.comparing(Seat::getColumn))
                .collect(Collectors.groupingBy(Seat::getRow));

        for (int rowNum : seatsByRow.keySet().stream().sorted().toList()) {
            List<Seat> rowSeats = seatsByRow.get(rowNum);
            if (rowSeats.size() < numberOfPassengers) {
                continue;
            }
            for (int i = 0; i <= rowSeats.size() - numberOfPassengers; i++) {
                List<Seat> potentialGroup = rowSeats.subList(i, i + numberOfPassengers);
                if (areSeatsConsecutive(potentialGroup)) {
                    return potentialGroup;
                }
            }
        }

        // For a single passanger return the first matching seat
        if (numberOfPassengers == 1) {
            return List.of(potentialSeats.getFirst());
        } else {
            return Collections.emptyList();
        }
    }

    /**
     * Checks if the seats in the provided list are consecutive based on their column character.
     * Assumes seats are within the same row.
     *
     * @param seats     -   list of Seat objects.
     * @return          -   true / false based on if seats are consecutive.
     */
    private boolean areSeatsConsecutive(List<Seat> seats) {
        if (seats == null || seats.size() < 2) {
            return true;
        }

        seats.sort(Comparator.comparing(s -> s.getColumn().charAt(0)));

        for (int i = 0; i < seats.size() - 1; i++) {
            char currentCol = seats.get(i).getColumn().charAt(0);
            char nextCol = seats.get(i + 1).getColumn().charAt(0);
            if (nextCol != currentCol + 1) {
                return false;
            }
        }
        return true;
    }
}
