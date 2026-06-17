<template>
  <v-sheet color="grey-lighten-4" rounded="lg" class="pa-2 d-flex align-center gap-2">
    <v-icon size="16" :color="active && !muted ? 'success' : 'grey'">
      {{ muted ? 'mdi-microphone-off' : 'mdi-microphone' }}
    </v-icon>
    <span class="text-caption font-weight-medium" :class="active ? (muted ? 'text-warning' : 'text-success') : 'text-disabled'" style="min-width:48px">
      {{ muted ? 'Muted' : active ? 'Live' : 'Inactive' }}
    </span>
    <v-progress-linear
      :model-value="active && !muted ? level : 0"
      :color="level > 75 ? 'warning' : 'success'"
      rounded height="8"
      class="flex-grow-1"
    />
    <span class="text-caption text-medium-emphasis" style="min-width:32px; text-align:right">
      {{ Math.round(level) }}%
    </span>
  </v-sheet>
</template>

<script setup>
defineProps({ level: Number, active: Boolean, muted: Boolean })
</script>
