<template>
  <v-card rounded="xl" elevation="0" border>
    <v-card-title class="text-body-1 font-weight-bold pt-4 px-4">
      <v-icon start color="primary">mdi-chart-bar</v-icon>
      Usage — Last 7 Days
    </v-card-title>
    <v-card-text class="px-4 pb-4">
      <div class="chart-container">
        <div class="chart-bars">
          <div v-for="(stat, i) in stats" :key="i" class="chart-col">
            <div class="bar-stack">
              <div class="bar bar--stt" :style="{ height: `${(stat.sttMinutes / maxStt) * 120}px` }" :title="`STT: ${stat.sttMinutes} min`" />
            </div>
            <span class="bar-label">{{ shortDate(stat.date) }}</span>
          </div>
        </div>
      </div>

      <v-divider class="my-4" />

      <v-row>
        <v-col cols="6" sm="3" v-for="metric in summaryMetrics" :key="metric.label">
          <div class="metric-box">
            <p class="text-h6 font-weight-bold">{{ metric.value }}</p>
            <p class="text-caption text-medium-emphasis">{{ metric.label }}</p>
          </div>
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

function shortDate(d) {
  return new Date(d).toLocaleDateString([], { weekday: 'short' })
}
</script>

<style scoped>
.chart-container { padding: 8px 0; }
.chart-bars { display: flex; align-items: flex-end; gap: 8px; height: 140px; }
.chart-col { flex: 1; display: flex; flex-direction: column; align-items: center; gap: 4px; }
.bar-stack { flex: 1; display: flex; flex-direction: column; justify-content: flex-end; width: 100%; }
.bar { width: 100%; border-radius: 6px 6px 0 0; min-height: 4px; transition: height 0.5s ease; }
.bar--stt { background: linear-gradient(180deg, #42A5F5, #1565C0); }
.bar-label { font-size: 11px; color: #888; font-weight: 500; }
.metric-box { padding: 12px; border-radius: 12px; background: rgba(0,0,0,0.03); text-align: center; }
</style>
