<template>
  <v-app>
    <v-app-bar color="primary" elevation="2">
      <v-app-bar-title>LMS Live Translation</v-app-bar-title>
      <template #append>
        <v-btn to="/admin" variant="text">Admin</v-btn>
        <v-btn to="/teacher" variant="text">Teacher</v-btn>
        <v-btn to="/student" variant="text">Student</v-btn>
      </template>
    </v-app-bar>
    <v-main>
      <router-view />
    </v-main>
  </v-app>
</template>

<script setup lang="ts">
</script>
