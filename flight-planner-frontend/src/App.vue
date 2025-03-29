<!-- App.vue -->
<template>
  <div class="app-layout"> <!-- This is the parent -->
    <FlightSearch />
  </div>
</template>

<script setup>
import FlightSearch from './components/FlightSearch.vue';
</script>

<style scoped>
.app-layout {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
}
</style>
