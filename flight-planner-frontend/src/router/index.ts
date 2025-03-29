import { createRouter, createWebHistory } from 'vue-router'
import FlightSearchView from '../views/FlightSearchView.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'home',
      component: FlightSearchView,
    },
  ],
})

export default router
