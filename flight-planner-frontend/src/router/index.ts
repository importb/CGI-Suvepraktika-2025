// src/router/index.js
import { createRouter, createWebHistory } from "vue-router";
import FlightSearchView from "../views/FlightSearchView.vue";

const router = createRouter({
    history: createWebHistory(import.meta.env.BASE_URL),
    routes: [
        {
            path: "/", // Or '/flights'
            name: "FlightSearch", // Or 'home'
            component: FlightSearchView,
        },
        {
            path: "/flights/seats/:flightId",
            name: "FlightSeats",
            component: () => import("../views/FlightSeatSelectionView.vue"),
            props: true,
        },
    ],
});

export default router;
