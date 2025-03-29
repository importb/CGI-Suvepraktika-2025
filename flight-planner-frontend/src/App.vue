<!-- src/App.vue -->
<template>
  <div id="app">
    <header>
      <router-link :to="{ name: 'FlightSearch' }">Flight Search</router-link>
    </header>

    <main>
      <router-view />
    </main>
  </div>
</template>

<script setup>
import { RouterView, RouterLink } from 'vue-router';
</script>

<style scoped>
#app header a {
  padding: 4px;
  border-radius: 4px;
  border: 2px solid #bdc4d4;
  background-color: #e5edff;
  color: black;
  text-decoration: none;
  font-weight: bold;
  font-size: 1.1em;
  transition: color 0.2s ease;
}
</style>