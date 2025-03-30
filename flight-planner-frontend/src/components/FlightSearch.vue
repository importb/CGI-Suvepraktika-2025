<template>
    <div class="flights-container">
        <h1>Flight Plan</h1>

        <!-- Filter Controls Section -->
        <div class="filters">
            <div class="filter-group">
                <label for="destination">Destination:</label>
                <input
                    id="destination"
                    type="text"
                    v-model="filterDestination"
                    placeholder="WAW"
                />
            </div>
            <div class="filter-group">
                <label for="date">Departure:</label>
                <input id="date" type="date" v-model="filterDate" />
            </div>
            <div class="filter-group">
                <label for="duration">Max Duration:</label>
                <input
                    id="duration"
                    type="number"
                    v-model.number="filterMaxDurationMinutes"
                    placeholder="180"
                    min="0"
                />
            </div>
            <div class="filter-group">
                <label for="price">Max Price (€):</label>
                <input
                    id="price"
                    type="number"
                    v-model.number="filterMaxPrice"
                    placeholder="250"
                    min="0"
                    step="0.01"
                />
            </div>
            <button @click="resetFilters" class="reset-button">
                Reset Filters
            </button>
        </div>

        <!-- Loading and Error States -->
        <div v-if="isLoading" class="loading">Loading flights...</div>
        <div v-if="error" class="error">Error loading flights: {{ error }}</div>

        <!-- Flights Data Display -->
        <div v-if="!isLoading && !error">
            <table v-if="filteredFlights.length > 0" class="flights-table">
                <thead>
                    <tr>
                        <th>Flight Nr</th>
                        <th>Origin</th>
                        <th>Destination</th>
                        <th>Departure</th>
                        <th>Arrival</th>
                        <th>Duration</th>
                        <th>Starting Price</th>
                        <th>Aircraft Type</th>
                    </tr>
                </thead>
                <tbody>
                    <!-- Interactive Flight Row - Navigates to Flight Details on Click -->
                    <tr v-for="flight in filteredFlights" :key="flight.id" @click="goToFlightDetails(flight)" class="flight-row" :data-flight-id="flight.id">
                        <td>{{ flight.flightNr }}</td>
                        <td>{{ flight.origin }}</td>
                        <td>{{ flight.destination }}</td>
                        <td>{{ formatDateTime(flight.departureTime) }}</td>
                        <td>{{ formatDateTime(flight.arrivalTime) }}</td>
                        <td>{{ formatDuration(flight.departureTime, flight.arrivalTime) }}</td>
                        <td>{{ formatPrice(flight.price) }}</td>
                        <td>{{ flight.aircraftType }}</td>
                    </tr>
                </tbody>
            </table>

            <!-- Empty State -->
            <div v-else class="no-flights">No flights found.</div>
        </div>
    </div>
</template>

<script setup>
import { ref, onMounted, computed } from "vue";
import { useRouter } from "vue-router";
import axios from "axios";

/**
 * Flights component manages the display and filtering of available flights.
 * 
 * This component:
 * - Fetches flight data from the API
 * - Provides filtering capabilities by destination, date, duration, and price
 * - Formats flight information for display
 * - Handles navigation to flight details page
 */

// State management
const flights = ref([]);
const isLoading = ref(false);
const error = ref(null);

// Filter state variables
const filterDestination = ref("");
const filterDate = ref("");
const filterMaxDurationMinutes = ref(null);
const filterMaxPrice = ref(null);

// Router for navigation
const router = useRouter();

// API configuration
const API_URL = "http://localhost:8080/api";

/**
 * Calculates the duration in minutes between two ISO datetime strings.
 * 
 * @param {string} startDateTimeString - ISO datetime string for departure
 * @param {string} endDateTimeString - ISO datetime string for arrival
 * @returns {number|null} - Duration in minutes or null if calculation fails
 */
const calculateDurationInMinutes = (
    startDateTimeString,
    endDateTimeString
) => {
    if (!startDateTimeString || !endDateTimeString) return null;
    try {
        const start = new Date(startDateTimeString);
        const end = new Date(endDateTimeString);
        const diffMillis = end.getTime() - start.getTime();

        if (isNaN(diffMillis) || diffMillis < 0) return null;

        return Math.floor(diffMillis / (1000 * 60));
    } catch (e) {
        console.error(
            "Error calculating duration in minutes:",
            startDateTimeString,
            endDateTimeString,
            e
        );
        return null;
    }
};

/**
 * Computed property that filters flights based on user-selected criteria.
 * Applies filters for destination, departure date, flight duration, and price.
 * 
 * @returns {Array} - Filtered array of flight objects
 */
const filteredFlights = computed(() => {
    return flights.value.filter((flight) => {
        // Match by destination (case-insensitive partial match)
        const destinationMatch =
            !filterDestination.value ||
            flight.destination
                .toLowerCase()
                .includes(filterDestination.value.toLowerCase());

        // Match by departure date (prefix match for ISO date string)
        const dateMatch =
            !filterDate.value ||
            (flight.departureTime &&
                flight.departureTime.startsWith(filterDate.value));

        // Match by maximum flight duration in minutes
        const durationMinutes = calculateDurationInMinutes(
            flight.departureTime,
            flight.arrivalTime
        );
        const durationMatch =
            filterMaxDurationMinutes.value === null ||
            filterMaxDurationMinutes.value === "" ||
            (durationMinutes !== null &&
                durationMinutes <= Number(filterMaxDurationMinutes.value));

        // Match by maximum price
        const priceMatch =
            filterMaxPrice.value === null ||
            filterMaxPrice.value === "" ||
            (flight.price !== null &&
                Number(flight.price) <= Number(filterMaxPrice.value));

        // Flight must match all filter criteria to be included
        return destinationMatch && dateMatch && durationMatch && priceMatch;
    });
});

/**
 * Resets all filter fields to their initial empty state.
 * Called when user clicks the reset button.
 */
const resetFilters = () => {
    filterDestination.value = "";
    filterDate.value = "";
    filterMaxDurationMinutes.value = null;
    filterMaxPrice.value = null;
};

/**
 * Fetches flight data from the backend API.
 * Updates component state based on API response.
 */
const fetchFlights = async () => {
    isLoading.value = true;
    error.value = null;

    try {
        const response = await axios.get(`${API_URL}/flights`);
        flights.value = response.data;
    } catch (err) {
        // Handle error with fallback message if specific error details aren't available
        error.value = err.response?.data?.message || err.message || 'Something went really wrong.'
        flights.value = [];
    } finally {
        isLoading.value = false;
    }
}

/** 
 * Formats ISO datetime string into a localized, user-friendly format.
 * 
 * @param {string} dateTimeString - ISO datetime string
 * @returns {string} - Formatted date and time string in local format
 */
const formatDateTime = (dateTimeString) => {
    if (!dateTimeString) return 'N/A';
    try {
        const date = new Date(dateTimeString);
        return date.toLocaleString(undefined, {
            year: 'numeric',
            month: '2-digit',
            day: '2-digit',
            hour: '2-digit',
            minute: '2-digit',
            hour12: false,
        });
    } catch (e) {
        console.error("Error formatting date:", dateTimeString, e);
        return dateTimeString;
    }
};

/**
 * Calculates and formats the duration between departure and arrival times.
 * Returns a human-readable string in the format "Xh Ym".
 * 
 * @param {string} startDateTimeString - ISO departure datetime string
 * @param {string} endDateTimeString - ISO arrival datetime string
 * @returns {string} - Formatted duration string
 */
const formatDuration = (startDateTimeString, endDateTimeString) => {
    if (!startDateTimeString || !endDateTimeString) return 'N/A';
    try {
        const start = new Date(startDateTimeString);
        const end = new Date(endDateTimeString);
        const diffMillis = end.getTime() - start.getTime();

        if (isNaN(diffMillis) || diffMillis < 0) return 'N/A';

        const totalMinutes = Math.floor(diffMillis / (1000 * 60));
        const hours = Math.floor(totalMinutes / 60);
        const minutes = totalMinutes % 60;

        let durationString = '';
        if (hours > 0) {
            durationString += `${hours}h `;
        }

        durationString += `${minutes}m`;
        return durationString.trim();
    } catch (e) {
        console.error("Error calculating duration:", startDateTimeString, endDateTimeString, e);
        return 'Error';
    }
};

/**
 * Formats numerical price value into a standardized currency string.
 * 
 * @param {number|string} price - Raw price value
 * @returns {string} - Formatted price with currency symbol and decimal places
 */
const formatPrice = (price) => {
    if (price === null || price === undefined) return "N/A";

    const numberPrice = Number(price);

    if (isNaN(numberPrice)) return "N/A";

    return `${numberPrice.toFixed(2)}€`;
};

/**
 * Navigates to the flight details page for seat selection.
 * Uses Vue Router to change the route.
 * 
 * @param {Object} flight - Flight object containing at minimum an id property
 */
const goToFlightDetails = (flight) => {
    if (!flight || !flight.id) {
        console.error("Cannot navigate: Flight data or ID is missing", flight);
        return;
    }

    if (flight.price === null || flight.price === undefined || isNaN(Number(flight.price))) {
         return;
    }

    router.push({
        path: `/flights/seats/${flight.id}`,
        query: { basePrice: flight.price }
    });
};

// Lifecycle hook: Fetch flight data when component is mounted
onMounted(() => {
    fetchFlights();
});
</script>

<style scoped>
/* Main container styling */
.flights-container {
    font-family: sans-serif;
    max-width: 1200px;
    margin: 20px auto;
    padding: 20px;
    border: 2px solid #bdc4d4;
    border-radius: 8px;
    background-color: #e5edff;
}

/* Page title */
h1 {
    text-align: center;
    color: black;
    margin-bottom: 25px;
}

/* Filter section styling */
.filters {
    display: flex;
    flex-wrap: wrap;
    gap: 15px;
    margin-bottom: 25px;
    padding: 15px;
    background-color: #d6e0f5;
    border-radius: 4px;
    align-items: flex-end;
}

/* Individual filter group styling */
.filter-group {
    display: flex;
    flex-direction: column;
    gap: 5px;
    flex: 1 1 150px;
    min-width: 150px;
}

.filter-group label {
    font-size: 0.9em;
    color: black;
    font-weight: bold;
    margin-bottom: 2px;
}

.filter-group input {
    padding: 8px 10px;
    border: 1px solid #bdc4d4;
    border-radius: 4px;
    font-size: 0.95em;
    width: 100%;
    box-sizing: border-box;
    height: 35px;
}

/* Reset button styling */
.reset-button {
    padding: 0 15px;
    background-color: #bdc4d4;
    font-weight: bold;
    color: black;
    border: none;
    border-radius: 4px;
    cursor: pointer;
    font-size: 0.95em;
    height: 35px;
    line-height: 35px;
    transition: background-color 0.2s ease;
    flex-shrink: 0;
}

.reset-button:hover {
    background-color: #5a6268;
}

/* State message styling */
.loading,
.error,
.no-flights {
    text-align: center;
    padding: 20px;
    margin-top: 20px;
    font-size: 1.1em;
}

.error {
    color: red;
    background-color: #f8d7da;
    border: 1px solid #f5c6cb;
    border-radius: 4px;
}

.no-flights {
    color: black;
}

/* Table styling */
.flights-table {
    width: 100%;
    border-collapse: separate;
    border-spacing: 0 5px;
    margin-top: 20px;
}

.flights-table th,
.flights-table td {
    border: none;
    padding: 10px 12px;
    text-align: center;
    font-size: 0.95em;
    color: black;
    vertical-align: middle;
}

.flights-table th {
    background-color: #d6e0f5;
    color: black;
    font-weight: bold;
}

/* Rounded corners for first and last cells in header and rows */
.flights-table thead th:first-child {
    border-top-left-radius: 4px;
    border-bottom-left-radius: 4px;
}

.flights-table thead th:last-child {
    border-top-right-radius: 4px;
    border-bottom-right-radius: 4px;
}

.flights-table tbody td {
    background-color: white;
}

.flights-table tbody td:first-child {
    border-top-left-radius: 4px;
    border-bottom-left-radius: 4px;
}

.flights-table tbody td:last-child {
    border-top-right-radius: 4px;
    border-bottom-right-radius: 4px;
}

/* Row hover effects */
.flights-table tbody tr {
    box-shadow: 0 2px 5px rgba(0, 0, 0, 0.1);
    transition: box-shadow 0.2s ease-in-out, transform 0.2s ease-in-out;
}

.flights-table tbody tr:hover {
    box-shadow: 0 4px 10px rgba(0, 0, 0, 0.35);
}
</style>