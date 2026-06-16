<template>
  <div class="audio-meter">
    <div class="meter-label">
      <v-icon size="16" :color="active ? 'success' : 'grey'">
        {{ muted ? 'mdi-microphone-off' : 'mdi-microphone' }}
      </v-icon>
      <span class="text-caption ml-1" :class="active ? 'text-success' : 'text-disabled'">
        {{ muted ? 'Muted' : active ? 'Live' : 'Inactive' }}
      </span>
    </div>
    <div class="bars">
      <div
        v-for="i in BAR_COUNT"
        :key="i"
        class="bar"
        :class="{
          'bar--active': active && !muted && (i / BAR_COUNT) * 100 <= level,
          'bar--warn': active && !muted && (i / BAR_COUNT) * 100 <= level && (i / BAR_COUNT) > 0.75,
        }"
      />
    </div>
    <span class="text-caption text-disabled ml-2">{{ Math.round(level) }}%</span>
  </div>
</template>

<script setup lang="ts">
const BAR_COUNT = 20

defineProps<{
  level: number
  active: boolean
  muted: boolean
}>()
</script>

<style scoped>
.audio-meter {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 12px;
  background: rgba(0,0,0,0.04);
  border-radius: 12px;
}
.meter-label {
  display: flex;
  align-items: center;
  min-width: 60px;
}
.bars {
  display: flex;
  gap: 2px;
  align-items: flex-end;
  height: 24px;
}
.bar {
  width: 4px;
  height: 100%;
  background: #e0e0e0;
  border-radius: 2px;
  transition: background 0.08s;
}
.bar--active {
  background: #4caf50;
}
.bar--warn {
  background: #ff9800;
}
</style>
