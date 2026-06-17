<template>
  <v-card rounded="xl" elevation="0" border>
    <v-card-title class="text-body-1 font-weight-bold pt-4 px-4">
      <v-icon start color="primary">mdi-chart-bar</v-icon>
      Usage — Last 7 Days
    </v-card-title>
    <v-card-text class="px-4 pb-4">

      <!-- Bar chart using flex + inline styles for dynamic heights only -->
      <v-sheet class="d-flex align-end gap-2 pa-2 rounded-lg" style="height:148px" color="grey-lighten-5">
        <div
          v-for="(stat, i) in stats" :key="i"
          class="d-flex flex-column align-center gap-1 flex-grow-1"
          style="height:100%"
        >
          <v-tooltip :text="`${shortDate(stat.date)}: ${stat.sttMinutes} min`">
            <template #activator="{ props: tip }">
              <div
                v-bind="tip"
                class="w-100 rounded-t align-self-end"
                style="background:linear-gradient(180deg, #42A5F5, #1565C0); min-height:4px; transition:height 0.5s ease"
                :style="`height:${(stat.sttMinutes / maxStt) * 120}px`"
              />
            </template>
          </v-tooltip>
          <span class="text-caption text-medium-emphasis" style="font-size:11px">{{ shortDate(stat.date) }}</span>
        </div>
      </v-sheet>

      <v-divider class="my-4" />

      <v-row>
        <v-col v-for="metric in summaryMetrics" :key="metric.label" cols="6" sm="3">
          <v-sheet color="grey-lighten-5" rounded="lg" class="pa-3 text-center">
            <p class="text-h6 font-weight-bold mb-0">{{ metric.value }}</p>
            <p class="text-caption text-medium-emphasis">{{ metric.label }}</p>
          </v-sheet>
        </v-col>
      </v-row>
    </v-card-text>
  </v-card>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({ stats: Array })

const maxStt = computed(() => Math.max(...props.stats.map(s => s.sttMinutes), 1))

const summaryMetrics = computed(() => {
  const total = props.stats.reduce((acc, s) => ({
    sttMinutes: acc.sttMinutes + s.sttMinutes,
    sessionCount: acc.sessionCount + s.sessionCount,
    translationChars: acc.translationChars + s.translationChars,
    estimatedCost: acc.estimatedCost + s.estimatedCost,
  }), { sttMinutes: 0, sessionCount: 0, translationChars: 0, estimatedCost: 0 })
  return [
    { label: 'Total Sessions', value: total.sessionCount },
    { label: 'STT Minutes', value: total.sttMinutes.toLocaleString() },
    { label: 'Translation Chars', value: (total.translationChars / 1000).toFixed(1) + 'K' },
    { label: 'Est. Cost', value: `$${total.estimatedCost.toFixed(2)}` },
  ]
})

function shortDate(d) { return new Date(d).toLocaleDateString([], { weekday: 'short' }) }
</script>
