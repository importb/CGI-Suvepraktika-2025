<template>
    <!-- 
        Button representing a single seat. 
        Applies dynamic classes based on seat state (occupied, selected, recommended).
        Displays indicators for special features (window, legroom, exit).
        Emits a 'select' event when clicked if the seat is available.
    -->
    <button
        class="seat"
        :class="seatClasses"
        :disabled="seat.occupied"
        @click="selectSeat"
        :aria-label="seatLabel"
        :title="seatLabel"
    >
        <!-- Indicators for seat features, positioned absolutely -->
        <span v-if="seat.window" class="indicator window">W</span>
        <span v-if="seat.hasExtraLegroom" class="indicator legroom">L</span>
        <span v-if="seat.nearExit" class="indicator exit">E</span>

        <!-- Display seat number only if it's not occupied -->
        <span v-if="!seat.occupied" class="seat-nr">{{ seat.seatNr }}</span>
    </button>
</template>

<script setup>
import { computed } from "vue";

/**
 * SeatComponent represents a single, clickable seat in the seat map.
 *
 * It receives seat data and selection/recommendation status as props,
 * calculates appropriate CSS classes and accessibility labels,
 * and emits an event when an available seat is clicked.
 */

/**
 * Component props definition.
 * @property {object} seat - The core data object for the seat, containing properties like seatNr, occupied, window, hasExtraLegroom, nearExit. Required.
 * @property {boolean} isSelected - Indicates if this seat is currently selected by the user. Defaults to false.
 * @property {boolean} isRecommended - Indicates if this seat is recommended based on user preferences. Defaults to false.
 */
const props = defineProps({
    seat: {
        type: Object,
        required: true,
        // Example structure: { seatNr: '1A', occupied: false, window: true, hasExtraLegroom: false, nearExit: false }
    },
    isSelected: {
        type: Boolean,
        default: false,
    },
    isRecommended: {
        type: Boolean,
        default: false,
    },
});

/**
 * Component emits definition.
 * @event select - Emitted when an available seat is clicked. Passes the seat object as payload.
 */
const emit = defineEmits(["select"]);

/**
 * Computes a dynamic object of CSS classes to apply to the seat button.
 * Classes reflect the seat's state (occupied, available, selected, recommended)
 * and its features (window, legroom, exit).
 * @returns {object} An object where keys are class names and values are booleans indicating if the class should be applied.
 */
const seatClasses = computed(() => ({
    occupied: props.seat.occupied,
    available: !props.seat.occupied,
    selected: props.isSelected,
    recommended: props.isRecommended && !props.isSelected && !props.seat.occupied,
    window: props.seat.window,
    legroom: props.seat.hasExtraLegroom,
    exit: props.seat.nearExit,
    firstClass: props.seat.firstClass
}));

/**
 * Computes a descriptive string for the seat, used for ARIA label and title attributes.
 * Provides information about the seat number, status (Occupied, Selected, Recommended),
 * and any special features.
 * @returns {string} A descriptive label for the seat.
 */
const seatLabel = computed(() => {
    let label = `Seat ${props.seat.seatNr}`;
    if (props.seat.occupied) return `${label} (Occupied)`; // If occupied, no further info needed.
    if (props.isSelected) label += " (Selected)";
    // Add recommended status only if it's recommended and not already selected.
    if (props.isRecommended && !props.isSelected) label += " (Recommended)";
    // Append feature descriptions.
    if (props.seat.window) label += " - Window";
    if (props.seat.hasExtraLegroom) label += " - Extra Legroom";
    if (props.seat.nearExit) label += " - Near Exit";
    if (props.seat.firstClass) label += " - First Class";
    return label;
});

/**
 * Computes whether the seat has any visual indicator (Window, Legroom, Exit).
 * This computed property was defined in the original code but doesn't seem to be used in the template.
 * It could be used, for example, to adjust padding or layout if indicators are present.
 * @returns {boolean} True if the seat has at least one feature indicator, false otherwise.
 */
const hasIndicator = computed(() => {
    return (
        props.seat.window || props.seat.hasExtraLegroom || props.seat.nearExit
    );
});

/**
 * Handles the click event on the seat button.
 * If the seat is not occupied, it emits the 'select' event, passing the
 * seat object data to the parent component.
 */
const selectSeat = () => {
    if (!props.seat.occupied) {
        emit("select", props.seat);
    }
};
</script>

<style scoped>
.seat {
    width: 35px;
    height: 35px;
    border: 1px solid black;
    border-radius: 5px;
    margin: 1px;
    cursor: pointer;
    display: flex;
    justify-content: center;
    align-items: center;
    font-size: 0.7em;
    font-weight: bold;
    position: relative;
    overflow: hidden;
    transition: all 0.2s ease;
    box-sizing: border-box;
    padding: 0;
    color: black;
}

.seat:hover:not(:disabled) {
    transform: scale(1.05);
    border-color: transparent;
}

.seat.available {
    background-color: #e5edff;
}

.seat.firstClass {
    background-color: rgb(255, 222, 161);
}


.seat.occupied {
    background-color: #5a6268;
    cursor: not-allowed;
    border-color: #5a6268;
}

.seat.recommended {
    border: 2px solid #ffc107;
    box-shadow: 0 0 5px rgba(255, 193, 7, 0.5);
}

.seat.selected {
    background-color: orange;
    color: white;
    border-color: orange;
}


.indicator {
    position: absolute;
    width: 12px;
    height: 12px;
    border-radius: 2px;
    font-size: 0.8em;
    line-height: 12px;
    text-align: center;
    color: black;
    font-weight: bold;
}

.indicator.window { background-color: #add8e6; top: 1px; left: 1px; }
.indicator.legroom { background-color: #90ee90; top: 1px; right: 1px; } 
.indicator.exit { background-color: #ffcccb; bottom: 1px; left: 1px; }


</style>
