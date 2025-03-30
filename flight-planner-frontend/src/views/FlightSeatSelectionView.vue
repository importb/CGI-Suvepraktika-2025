<template>
    <div class="seat-selection-container">
        <h1>Select Your Seats</h1>
        <!-- Loading and error states for the entire component -->
        <div v-if="isLoading" class="loading">
            Loading flight details and seat map...
        </div>
        <div v-if="error" class="error">Error loading data: {{ error }}</div>

        <!-- Main content area, only shown when data is available -->
        <div v-if="!isLoading && !error && flightDetails && seatMapData" class="content">
            <!-- Flight information section -->
            <div class="flight-info">
                <h2>Flight {{ flightDetails.flightNr }}</h2>
                <p>
                    <span>From:
                        <strong>{{ flightDetails.origin }}</strong>
                    </span>
                    <span>To:
                        <strong>{{ flightDetails.destination }}</strong>
                    </span>
                </p>
                <p>
                    <span>Departure:
                        <strong>
                            {{ formatDateTime(flightDetails.departureTime) }}
                        </strong>
                    </span>
                    <span>Arrival:
                        <strong>
                            {{ formatDateTime(flightDetails.arrivalTime) }}
                        </strong>
                    </span>
                </p>
                <p>
                    <span>Duration:
                        <strong>
                            {{formatDuration(flightDetails.departureTime, flightDetails.arrivalTime)}}
                        </strong>
                    </span>
                    <span>Aircraft:
                        <strong>
                            {{ flightDetails.aircraftType }}
                        </strong>
                    </span>
                </p>
                <p>
                    <span>Starting Seat Price:
                        <strong>
                            {{ flightDetails.price }}€
                        </strong>
                    </span>
                </p>
            </div>

            <!-- User preferences section for filtering seat recommendations -->
            <div class="preferences">
                <h3>Preferences</h3>
                <div class="pref-group">
                    <label for="passengers">Passengers:</label>
                    <input id="passengers" type="number" v-model.number="numberOfPassengers" min="1" max="10"/>
                </div>
                <div class="pref-group">
                    <label>
                        <input type="checkbox" v-model="preferWindow" />
                        Window Seat
                    </label>
                </div>
                <div class="pref-group">
                    <label>
                        <input type="checkbox" v-model="preferExtraLegroom" />
                        Extra Legroom
                    </label>
                </div>
                <div class="pref-group">
                    <label>
                        <input type="checkbox" v-model="preferNearExit" />
                        Near Exit
                    </label>
                </div>

                <!-- Manually trigger preference-based seat map update -->
                <button @click="fetchSeatMapAndUpdate" class="update-button">
                    Update Recommendations
                </button>
            </div>

            <!-- Seat map visualization area -->
            <div class="seat-map-area">
                <!-- Loading and error states for just the seat map -->
                <div v-if="isSeatMapLoading" class="loading small">
                    Updating seat map...
                </div>
                <div v-if="seatMapError" class="error small">
                    Error updating seat map: {{ seatMapError }}
                </div>

                <div v-if="!isSeatMapLoading">
                    <h3>Seat Map</h3>
                    <!-- Legend explaining seat map symbols and colors -->
                    <div class="legend">
                        <span>
                            <span class="seat available"></span> Available
                        </span>
                        <span>
                            <span class="seat occupied"></span> Occupied
                        </span>
                        <span>
                            <span class="seat recommended"></span> Recommended
                        </span>
                        <span>
                            <span class="seat selected"></span> Selected
                        </span>
                        <span>
                            <span class="seat window">W</span> Window
                        </span>
                        <span>
                            <span class="seat legroom">L</span> Legroom
                        </span>
                        <span>
                            <span class="seat exit">E</span> Exit
                        </span>
                        <span>
                            <span class="seat firstClass"></span> First Class +50€
                        </span>
                    </div>

                    <!-- Dynamic CSS Grid for seat map -->
                    <div class="seat-map" :style="seatMapGridStyle">
                        <!-- Column Headers (A, B, C, etc.) -->
                        <div class="grid-cell header"></div>
                        <div v-for="col in seatMapData.columns" :key="`header-&{col}`" class="grid-cell header col-header">
                            {{ col }}
                        </div>

                        <!-- Rows and Seats -->
                        <template v-for="row in seatMapData.totalRows" :key="`row-${row}`">
                            <!-- Row number -->
                            <div class="grid-cell header row-header">
                                {{ row }}
                            </div>
                            <!-- Individual seats in this row -->
                            <div v-for="col in seatMapData.columns" :key="`seat-${row}-${col}`" class="grid-cell">
                                <SeatComponent
                                    v-if="getSeat(row, col)"
                                    :seat="getSeat(row, col)"
                                    :is-recommended="isRecommended(row, col)"
                                    :is-selected="isSelected(row, col)"
                                    @select="handleSeatSelect"
                                />
                                <div v-else class="seat empty"></div>
                            </div>
                        </template>
                    </div>
                </div>
            </div>

            <!-- Selection summary and confirmation area -->
            <div class="selection-summary">
                <h3>
                    Your Selection ({{ selectedSeats.length }} /
                    {{ numberOfPassengers }})
                </h3>
                <ul v-if="selectedSeats.length > 0">
                    <!-- Display selected seat numbers -->
                    <li v-for="seat in selectedSeats" :key="seat.seatNr">
                        Seat {{ seat.seatNr }}
                        <span v-if="seat.firstClass" class="first-class-tag">
                            (First Class)
                        </span>
                        <button @click="deselectSeat(seat.seatNr)" class="deselect-button" aria-label="Deselect seat">
                            &times;
                        </button>
                    </li>
                </ul>
                <p v-else>Click on available seats in the map to select.</p>

                <!-- Dynamic Pricing Display -->
                <div class="pricing-details">
                    <h4>Price Breakdown</h4>
                    <p>
                        Base Price per Passenger:
                        <strong>{{ formatPrice(basePricePerPassenger) }}</strong>
                    </p>
                    <p>
                        Passengers:
                        <strong>{{ numberOfPassengers }}</strong>
                    </p>
                    <p v-if="firstClassSeatsCount > 0">
                        First Class Surcharge:
                        <strong>
                            {{ firstClassSeatsCount }} x {{ formatPrice(FIRST_CLASS_SURCHARGE) }} =
                            {{ formatPrice(firstClassSurchargeTotal) }}
                        </strong>
                    </p>
                    <hr />
                    <p class="total-price">
                        Total Price:
                        <strong>{{ formatPrice(totalPrice) }}</strong>
                    </p>
                </div>

                <button :disabled="selectedSeats.length !== numberOfPassengers" class="confirm-button" @click="confirmSelection">
                    Confirm Selection & Proceed
                </button>
            </div>
        </div>
    </div>
</template>

<script setup>
import { ref, onMounted, computed, watch } from "vue";
import axios from "axios";
import SeatComponent from "../components/SeatComponent.vue";

/**
 * SeatSelection component allows users to view flight details,
 * select seats based on preferences, and confirm their selection.
 *
 * This component:
 * - Fetches flight details and seat map data based on a flight ID.
 * - Displays flight information.
 * - Allows users to specify the number of passengers and seat preferences.
 * - Renders an interactive seat map.
 * - Highlights recommended seats based on preferences.
 * - Manages the user's seat selection process.
 * - Provides feedback during loading and error states.
 */

/**
 * Component props
 * @property {string|number} flightId - The unique identifier for the flight, passed via route params or props.
 */
const props = defineProps({
    flightId: {
        type: [String, Number],
        required: true,
    },
});

// State management using Vue 3 Composition API refs

// Loading and error states for the component and seat map
const isLoading = ref(true);
const error = ref(null);
const isSeatMapLoading = ref(false);
const seatMapError = ref(null);

// Core data references
const flightDetails = ref(null);
const seatMapData = ref(null);
const numberOfPassengers = ref(1);
const selectedSeats = ref([]);

// User preferences for seat recommendation
const preferWindow = ref(false);
const preferExtraLegroom = ref(false);
const preferNearExit = ref(false);

// Base API URL
const API_URL = "http://localhost:8080/api";

// This ideally needs to be moved to the backend :)
const FIRST_CLASS_SURCHARGE = 50;

/**
 * Base price per passenger, extracted from flightDetails.
 * Returns 0 if details are not loaded or price is missing/invalid.
 */
 const basePricePerPassenger = computed(() => {
    const price = flightDetails.value?.price;
    return price !== null && price !== undefined && !isNaN(Number(price))
        ? Number(price)
        : 0;
});


/**
 * Creates a lookup map (Map object) for fast seat access by seat number (e.g., "1A").
 * This optimizes retrieving seat details from O(n) (searching the array) to O(1) (map lookup).
 * The map is recomputed whenever the seatMapData changes.
 * @returns {Map<string, object>} A map where keys are seat numbers and values are seat objects.
 */
const seatsLookup = computed(() => {
    // Return an empty map if seat data isn't available yet.
    if (!seatMapData.value || !seatMapData.value.allSeats) {
        return new Map();
    }
    // Create and populate the map.
    const map = new Map();
    seatMapData.value.allSeats.forEach((seat) => {
        map.set(seat.seatNr, seat);
    });
    return map;
});

/**
 * Computes the CSS `grid-template-columns` style for the seat map layout.
 * Creates a dynamic grid based on the number of columns defined in seatMapData,
 * plus one extra column for the row number headers.
 * @returns {object} A style object containing the grid-template-columns property.
 */
const seatMapGridStyle = computed(() => {
    // Return empty style if data is not available.
    if (!seatMapData.value || !seatMapData.value.columns) {
        return {};
    }
    // Calculate the total number of columns needed in the grid.
    const gridCols = seatMapData.value.columns.length + 1; // +1 for row headers
    return {
        // 'auto' for the row header column, '1fr' for each seat column to distribute space evenly.
        "grid-template-columns": `auto repeat(${gridCols - 1}, 1fr)`,
    };
});

/**
 * Counts the number of selected seats that are designated as first class.
 * @returns {number} Count of selected first-class seats.
 */
 const firstClassSeatsCount = computed(() => {
    // Ensure selectedSeats contains objects with a 'firstClass' property
    return selectedSeats.value.filter((seat) => seat && seat.firstClass)
        .length;
});

/**
 * Calculates the total surcharge amount based on the number of selected first-class seats.
 * @returns {number} Total surcharge in Euros.
 */
const firstClassSurchargeTotal = computed(() => {
    return firstClassSeatsCount.value * FIRST_CLASS_SURCHARGE;
});

/**
 * Calculates the final total price for the selected seats and passengers.
 * @returns {number} The total price.
 */
const totalPrice = computed(() => {
    if (basePricePerPassenger.value <= 0) return 0;

    const passengerBaseCost =
        basePricePerPassenger.value * numberOfPassengers.value;
    const total = passengerBaseCost + firstClassSurchargeTotal.value;

    return total;
});

/**
 * Formats an ISO 8601 date-time string into a more readable, localized format.
 * Example: "2023-10-27T10:00:00Z" -> "Oct 27, 2023, 10:00 AM" (depends on locale)
 * @param {string} dateTimeString - The ISO date string to format.
 * @returns {string} The formatted date-time string, or 'N/A' if input is invalid/missing.
 */
const formatDateTime = (dateTimeString) => {
    if (!dateTimeString) return "N/A";
    try {
        // Use Intl.DateTimeFormat options for better control and localization.
        return new Date(dateTimeString).toLocaleString(undefined, {
            year: "numeric",
            month: "short",
            day: "numeric",
            hour: "numeric",
            minute: "2-digit",
            hour12: true,
        });
    } catch {
        // Return a fallback string if parsing fails.
        return "Invalid Date";
    }
};

/**
 * Calculates the duration between two ISO date-time strings and formats it.
 * Example: Calculates difference between arrival and departure time.
 * @param {string} start - The starting ISO date-time string (e.g., departure time).
 * @param {string} end - The ending ISO date-time string (e.g., arrival time).
 * @returns {string} Formatted duration string like "Xh Ym", or 'N/A' if input is invalid.
 */
const formatDuration = (start, end) => {
    if (!start || !end) return "N/A";
    try {
        const startDate = new Date(start);
        const endDate = new Date(end);
        const diffMs = endDate - startDate; // Difference in milliseconds.

        // Check for invalid dates or negative duration.
        if (isNaN(diffMs) || diffMs < 0) return "N/A";

        // Convert milliseconds to hours and minutes.
        const hours = Math.floor(diffMs / (1000 * 60 * 60));
        const minutes = Math.floor((diffMs % (1000 * 60 * 60)) / (1000 * 60));

        // Construct the formatted string.
        return `${hours}h ${minutes}m`;
    } catch {
        // Return fallback on any error during date processing.
        return "N/A";
    }
};

/**
 * Formats a numerical price value into a currency string with a Euro symbol.
 * Ensures two decimal places.
 * @param {number|string} price - The price value to format.
 * @returns {string} Formatted price string (e.g., "123.45€"), or 'N/A' if input is invalid/missing.
 */
const formatPrice = (price) => {
    // Handle null, undefined, or non-numeric input gracefully.
    if (price === null || price === undefined) return "N/A";
    const numberPrice = Number(price);
    if (isNaN(numberPrice)) return "N/A";

    // Format to 2 decimal places and append the Euro symbol.
    return `${numberPrice.toFixed(2)}€`;
};

/**
 * Retrieves a specific seat object from the pre-computed `seatsLookup` map.
 * Uses the row and column identifiers to construct the seat number key (e.g., "1A").
 * @param {string|number} row - The row identifier (e.g., 1, 2).
 * @param {string} col - The column identifier (e.g., 'A', 'B').
 * @returns {object|undefined} The seat object if found, otherwise undefined.
 */
const getSeat = (row, col) => {
    const seatNr = `${row}${col}`;
    return seatsLookup.value.get(seatNr);
};

/**
 * Checks if a specific seat is included in the list of recommended seats.
 * Recommended seats are determined by the backend based on user preferences.
 * @param {string|number} row - The row identifier of the seat.
 * @param {string} col - The column identifier of the seat.
 * @returns {boolean} True if the seat is recommended, false otherwise or if data is unavailable.
 */
const isRecommended = (row, col) => {
    // Ensure recommendation data exists before checking.
    if (!seatMapData.value || !seatMapData.value.recommendedSeatNrs) {
        return false;
    }
    const seatNr = `${row}${col}`;

    return seatMapData.value.recommendedSeatNrs.includes(seatNr);
};

/**
 * Checks if a specific seat is currently in the user's `selectedSeats` array
 * by comparing seat numbers within the objects.
 * @param {string|number} row - The row identifier of the seat.
 * @param {string} col - The column identifier of the seat.
 * @returns {boolean} True if the seat is currently selected by the user, false otherwise.
 */
 const isSelected = (row, col) => {
    const seatNr = `${row}${col}`;
    // Correct: Check if any object in the array has this seatNr
    return selectedSeats.value.some(
        (selectedSeat) => selectedSeat && selectedSeat.seatNr === seatNr
    );
};

/**
 * Asynchronously fetches detailed information for the specific flight ID from the API.
 * Updates the `flightDetails` ref on success or sets the `error` ref on failure.
 * Throws the error to be caught by the calling function (e.g., in onMounted).
 */
const fetchFlightDetails = async () => {
    try {
        // Make GET request to the specific flight endpoint.
        const response = await axios.get(`${API_URL}/flights/${props.flightId}`);
        flightDetails.value = response.data;
        error.value = null;
    } catch (err) {
        console.error("Error fetching flight details:", err);
        error.value = err.response?.data?.message || err.message || "Something went really wrong.";
        flightDetails.value = null;
        throw err;
    }
};

/**
 * Asynchronously fetches the seat map data for the flight from the API.
 * Includes current user preferences (passenger count, window, legroom, exit) as query parameters
 * to get tailored recommendations from the backend.
 * Updates `seatMapData` on success or sets `error`/`seatMapError` on failure.
 * Throws the error to be caught by the calling function.
 */
const fetchSeatMap = async () => {
    // Build query parameters based on current state refs.
    const params = new URLSearchParams();
    params.append("numberOfPassengers", numberOfPassengers.value);
    if (preferWindow.value) params.append("preferWindow", "true");
    if (preferExtraLegroom.value) params.append("preferExtraLegroom", "true");
    if (preferNearExit.value) params.append("preferNearExit", "true");

    try {
        // Make GET request to the flight's seats endpoint with parameters.
        const response = await axios.get(
            `${API_URL}/flights/${props.flightId}/seats`,
            { params }
        );
        seatMapData.value = response.data; // Store the received seat map data.
        seatMapError.value = null; // Clear previous seat map errors.
    } catch (err) {
        console.error("Error fetching seat map:", err);
        // Construct an appropriate error message.
        const message = err.response?.data?.message || err.message || "Could not load seat information.";

        // Set the error state: use the main 'error' ref during initial load,
        // otherwise use 'seatMapError' for subsequent updates.
        if (isLoading.value) {
            error.value = message;
        } else {
            seatMapError.value = message;
        }

        seatMapData.value = null;
        throw err;
    }
};

/**
 * Initiates an update of the seat map, typically triggered by changes in user preferences
 * or clicking the "Update Recommendations" button.
 * Sets loading indicators, clears previous recommendations, fetches new data, and handles errors.
 */
const fetchSeatMapAndUpdate = async () => {
    if (!flightDetails.value) return;

    isSeatMapLoading.value = true;
    seatMapError.value = null;

    // Clear existing recommendations immediately to provide visual feedback
    // that the recommendations are being recalculated.
    if (seatMapData.value) {
        seatMapData.value.recommendedSeatNrs = [];
    }

    try {
        // Call the main function to fetch seat map data with current preferences.
        await fetchSeatMap();
    } catch (err) {
        // Error is already handled within fetchSeatMap (setting seatMapError).
        console.error("Failed to update seat map:", err);
    } finally {
        isSeatMapLoading.value = false;
    }
};

/**
 * Handles the click event on a SeatComponent.
 * Toggles the selection state of the clicked seat.
 * - If the seat is already selected, it deselects it.
 * - If the seat is not selected and available (not occupied), it selects it,
 *   but only if the number of selected seats is less than `numberOfPassengers`.
 * @param {object} seat - The seat object passed from the SeatComponent's 'select' event.
 */
const handleSeatSelect = (seat) => {
    if (!seat || seat.occupied || !seat.seatNr) return;

    const seatNr = seat.seatNr;
    const index = selectedSeats.value.findIndex((s) => s.seatNr === seatNr);

    if (index > -1) {
        selectedSeats.value.splice(index, 1);
    } else {
        if (selectedSeats.value.length < numberOfPassengers.value) {
            selectedSeats.value.push(seat);
        }
    }
};


/**
 * Removes a specific seat object from the `selectedSeats` array based on its seat number.
 * Typically called when the user clicks the 'x' button next to a selected seat in the summary.
 * @param {string} seatNr - The identifier (e.g., "1A") of the seat to deselect.
 */
 const deselectSeat = (seatNr) => {
    // Correct: Find the index of the object with the matching seatNr
    const index = selectedSeats.value.findIndex(
        (selectedSeat) => selectedSeat && selectedSeat.seatNr === seatNr
    );
    if (index > -1) {
        selectedSeats.value.splice(index, 1);
    }
};

/**
 * Placeholder function for confirming the seat selection.
 * In a real application, this would likely:
 * 1. Validate the selection (correct number of seats).
 * 2. Send the selected seat numbers and flight ID to a backend API endpoint
 *    to reserve or book the seats.
 * 3. Navigate the user to the next step (e.g., payment, confirmation page).
 * 4. Handle potential errors from the backend (e.g., seats became unavailable).
 */
const confirmSelection = () => {
    const selectedSeatNumbers = selectedSeats.value.map((s) => s.seatNr);
 
    const bookingData = {
        flightId: props.flightId,
        passengers: numberOfPassengers.value,
        selectedSeats: selectedSeatNumbers,
        totalPrice: totalPrice.value,
    };

    console.log("Booking Data:", bookingData);
    alert(
        `Seats selected for Flight ${
            flightDetails.value?.flightNr
        }: ${selectedSeatNumbers.join(", ")}\nTotal Price: ${formatPrice(
            totalPrice.value
        )}\n\nProceeding to booking... (Placeholder)`
    );
};

/**
 * Vue Composition API lifecycle hook. Called after the component instance has been mounted.
 * Fetches initial flight details and seat map data sequentially.
 * Sets the overall loading state and handles initial errors.
 */
onMounted(async () => {
    isLoading.value = true;
    error.value = null;
    try {
        // Fetch flight details first. If this fails, error will be set, and
        // fetchSeatMap won't be called due to the check below.
        await fetchFlightDetails();

        // Fetch the seat map if flight details were loaded successfully.
        if (!error.value) {
            await fetchSeatMap();
        }
    } catch (err) {
        // Errors from fetchFlightDetails or fetchSeatMap are caught here.
        // The error state (error ref) should already be set by the failing function.
        console.error("Error during component mount:", err);
    } finally {
        isLoading.value = false;
    }
});

/**
 * Vue Composition API watcher.
 * Monitors changes in the user's seat preferences (window, legroom, exit).
 * When any of these preferences change, it triggers an update of the seat map
 * to fetch new recommendations from the backend.
 */
watch(
    [preferWindow, preferExtraLegroom, preferNearExit],
    () => {
        fetchSeatMapAndUpdate();
    },
    { deep: false }
);

/**
 * Vue Composition API watcher.
 * Monitors changes in the `numberOfPassengers` ref.
 * When the count changes:
 * 1. Resets the current `selectedSeats` array because the selection criteria changed.
 * 2. Triggers `fetchSeatMapAndUpdate` to get recommendations suitable for the new group size.
 * 3. If the new passenger count is lower than the old one, it trims the `selectedSeats`
 *    array just in case (though it's reset above, this is belt-and-suspenders).
 * @param {number} newCount - The new value of numberOfPassengers.
 * @param {number} oldCount - The previous value of numberOfPassengers.
 */
watch(numberOfPassengers, (newCount, oldCount) => {
    // Only act if the value actually changed.
    if (newCount !== oldCount) {
        // Reset selection as the required number of seats has changed.
        selectedSeats.value = [];

        // Fetch updated recommendations based on the new passenger count.
        fetchSeatMapAndUpdate();

        // Defensive check: If somehow seats were selected before reset, trim if needed.
        // This line might be redundant due to the reset above but ensures consistency.
        if (selectedSeats.value.length > newCount) {
            selectedSeats.value = selectedSeats.value.slice(0, newCount);
        }
    }
});
</script>

<style scoped>
/* Main container styling */
.seat-selection-container {
    font-family: sans-serif;
    max-width: 900px;
    margin: 20px auto;
    padding: 20px;
    border: 2px solid #bdc4d4;
    border-radius: 8px;
    background-color: #e5edff;
}

/* Heading styles */
h1, h2, h3 {
    text-align: center;
    color: black;
    margin-bottom: 15px;
}

/* Flight info section styling */
.flight-info h2 {
    margin-top: 0;
    margin-bottom: 10px;
    font-size: 1.4em;
}
.flight-info p {
    margin: 8px 0;
    line-height: 1.5;
    color: #303030;
    display: flex;
    flex-wrap: wrap;
    gap: 5px 20px;
}
.flight-info p span {
    display: inline-block;
}
.flight-info strong {
    color: black;
    font-weight: bold;
}

/* Loading and error states */
.loading,
.error {
    text-align: center;
    padding: 40px;
    font-size: 1.2em;
}

.error {
    color: red;
    background-color: #f8d7da;
    border: 1px solid #f5c6cb;
    border-radius: 4px;
}

/* Main content layout */
.content {
    display: flex;
    flex-direction: column;
    gap: 30px;
}

/* Section styling */
.flight-info {
    background-color: #d6e0f5;
    padding: 10px;
    border-radius: 5px;
}

/* Preferences section */
.preferences {
    background-color: #d6e0f5;
    padding: 15px;
    border-radius: 5px;
    display: flex;
    flex-wrap: wrap;
    gap: 15px;
    align-items: center;
    color: black;
}

.preferences h3 {
    width: 100%;
    text-align: left;
    margin-bottom: 10px;
}

.pref-group {
    display: flex;
    align-items: center;
    gap: 5px;
}

.pref-group label {
    cursor: pointer;
    display: flex;
    align-items: center;
    gap: 4px;
}

.pref-group input[type="number"] {
    width: 60px;
    height: 25px;
    padding: 5px;
    border: 1px solid black;
    border-radius: 3px;
}

/* Button styling */
.update-button,
.confirm-button,
.deselect-button {
    padding: 8px 15px;
    border: none;
    border-radius: 4px;
    cursor: pointer;
    font-weight: bold;
    transition: background-color 0.2s ease;
}

.update-button {
    background-color: #bdc4d4;
    color: black;
    margin-left: auto;
}
.update-button:hover {
    background-color: #5a6268;
}

.confirm-button {
    background-color: #bdc4d4;
    color: black;
    width: 100%;
    margin-top: 15px;
}
.confirm-button:hover:not(:disabled) {
    background-color: #5a6268;
}
.confirm-button:disabled {
    cursor: not-allowed;
}

.deselect-button {
    background-color: red;
    color: white;
    padding: 2px 6px;
    font-size: 0.8em;
    line-height: 1;
    margin-left: 8px;
}
.deselect-button:hover {
    background-color: darkred;
}

/* Seat map area styling */
.seat-map-area {
    background-color: #d6e0f5;
    padding: 20px;
    border-radius: 5px;
    overflow-x: auto;
}

/* Legend styling */
.legend {
    display: flex;
    flex-wrap: wrap;
    gap: 15px;
    margin-bottom: 20px;
    font-size: 0.9em;
    justify-content: center;
    color: black;
}

.legend > span {
    display: inline-flex;
    align-items: center;
    gap: 5px;
}

.legend .seat {
    display: inline-block;
    width: 18px;
    height: 18px;
    border: 1px solid black;
    border-radius: 3px;
    text-align: center;
    line-height: 16px;
    font-size: 0.7em;
    font-weight: bold;
}

/* Seat status colors */
.legend .available { background-color: #e5edff; }
.legend .occupied { background-color: #5a6268; color: white; }
.legend .recommended { border: 2px solid #ffc107; background-color: #e5edff; }
.legend .selected { background-color: orange; color: white; border-color: orange; }
.legend .window { background-color: #add8e6; }
.legend .legroom { background-color: #90ee90; }
.legend .exit { background-color: #ffcccb; }
.legend .firstClass { background-color: rgb(255, 222, 161); }

/* Seat map grid layout */
.seat-map {
    display: grid;
    gap: 5px;
    justify-items: center;
    align-items: center;
    min-width: 400px;
    padding: 10px;
}

.grid-cell {
    min-width: 35px;
    min-height: 35px;
    display: flex;
    justify-content: center;
    align-items: center;
}

.grid-cell.header {
    font-weight: bold;
    color: black;
    font-size: 0.9em;
}

/* Selection summary styling */
.selection-summary {
    background-color: #d6e0f5;
    padding: 15px;
    border-radius: 5px;
}

.selection-summary p {
    color: black;
}

.selection-summary ul {
    list-style: none;
    padding: 0;
    margin: 10px 0;
}

.selection-summary li {
    background-color: white;
    padding: 5px 10px;
    margin-bottom: 5px;
    border-radius: 3px;
    display: flex;
    justify-content: space-between;
    align-items: center;
    color: black;
}

.selection-summary .first-class-tag {
    color: black;
    font-style: italic;
}

.pricing-details h4 {
    color: black;
    font-weight: bold;
}
</style>