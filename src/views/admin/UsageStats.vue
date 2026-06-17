<template>
  <v-container fluid class="pa-6">
    <div class="d-flex align-center mb-6">
      <div>
        <h1 class="text-h5 font-weight-bold">Usage Statistics</h1>
        <p class="text-body-2 text-medium-emphasis">Hours spoken per class and workspace — monthly breakdown</p>
      </div>
      <v-spacer />
      <div class="d-flex gap-2 align-center">
        <v-select
          v-model="selectedWorkspaceId"
          :items="workspaceItems"
          label="Workspace"
          density="compact"
          style="min-width:180px"
          hide-details
          prepend-inner-icon="mdi-domain"
        />
        <v-select
          v-model="selectedMonth"
          :items="monthItems"
          label="Month"
          density="compact"
          style="min-width:160px"
          hide-details
          prepend-inner-icon="mdi-calendar-month"
        />
      </div>
    </div>

    <!-- Workspace KPI row -->
    <v-row class="mb-6">
      <v-col v-for="kpi in kpis" :key="kpi.label" cols="6" sm="3">
        <v-card rounded="xl" elevation="0" border class="pa-4 text-center">
          <v-icon :color="kpi.color" size="28" class="mb-1">{{ kpi.icon }}</v-icon>
          <p class="text-h5 font-weight-bold">{{ kpi.value }}</p>
          <p class="text-caption text-medium-emphasis">{{ kpi.label }}</p>
        </v-card>
      </v-col>
    </v-row>

    <!-- Classes breakdown table -->
    <v-card rounded="xl" elevation="0" border class="mb-6">
      <v-card-title class="text-body-1 font-weight-bold pt-4 px-4">
        <v-icon start color="primary">mdi-google-classroom</v-icon>
        Classes — {{ selectedMonthLabel }}
      </v-card-title>
      <v-card-text class="px-0 pb-0">
        <v-table>
          <thead>
            <tr>
              <th class="text-left pl-6">Class</th>
              <th class="text-right">Sessions</th>
              <th class="text-right">Total Time</th>
              <th class="text-right">Avg / Session</th>
              <th class="text-right pr-6">Participants</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="row in classRows" :key="row.cls.id" class="table-row">
              <td class="pl-6 py-3">
                <div class="d-flex align-center gap-2">
                  <v-avatar color="teal" size="32" rounded="lg">
                    <v-icon color="white" size="16">mdi-google-classroom</v-icon>
                  </v-avatar>
                  <div>
                    <p class="text-body-2 font-weight-bold mb-0">{{ row.cls.name }}</p>
                    <p class="text-caption text-medium-emphasis">{{ row.cls.description }}</p>
                  </div>
                </div>
              </td>
              <td class="text-right">
                <v-chip size="small" color="primary" variant="tonal">{{ row.sessionCount }}</v-chip>
              </td>
              <td class="text-right">
                <span class="font-weight-bold">{{ formatHours(row.totalMinutes) }}</span>
              </td>
              <td class="text-right text-medium-emphasis text-caption">
                {{ row.sessionCount > 0 ? formatMins(Math.round(row.totalMinutes / row.sessionCount)) : '—' }}
              </td>
              <td class="text-right pr-6">{{ row.participantCount }}</td>
            </tr>
            <tr v-if="!classRows.length">
              <td colspan="5" class="text-center py-8 text-disabled">No data for this period</td>
            </tr>
          </tbody>
          <tfoot v-if="classRows.length">
            <tr class="total-row">
              <td class="pl-6 py-3 font-weight-bold">TOTAL — {{ selectedWorkspace?.name }}</td>
              <td class="text-right font-weight-bold">{{ totals.sessionCount }}</td>
              <td class="text-right font-weight-bold text-primary">{{ formatHours(totals.totalMinutes) }}</td>
              <td class="text-right"></td>
              <td class="text-right pr-6 font-weight-bold">{{ totals.participantCount }}</td>
            </tr>
          </tfoot>
        </v-table>
      </v-card-text>
    </v-card>

    <!-- Monthly trend chart per class -->
    <v-card rounded="xl" elevation="0" border>
      <v-card-title class="text-body-1 font-weight-bold pt-4 px-4">
        <v-icon start color="primary">mdi-chart-line</v-icon>
        Monthly Trend — All Classes
      </v-card-title>
      <v-card-text class="px-4 pb-4">
        <div class="trend-chart">
          <div v-for="cls in workspaceClasses" :key="cls.id" class="trend-row mb-4">
            <div class="trend-label text-caption font-weight-bold">{{ cls.name }}</div>
            <div class="trend-bars">
              <div v-for="m in allMonths" :key="m.value" class="trend-bar-col">
                <div
                  class="trend-bar"
                  :style="{ height: `${barHeight(cls.id, m.value)}px`, background: barColor(cls.id) }"
                  :title="`${m.label}: ${formatHours(monthStat(cls.id, m.value).totalMinutes)}`"
                />
                <span class="trend-bar-label">{{ m.shortLabel }}</span>
              </div>
            </div>
          </div>
        </div>
        <div class="d-flex flex-wrap gap-3 mt-4">
          <div v-for="cls in workspaceClasses" :key="cls.id" class="legend-item">
            <span class="legend-dot" :style="{ background: barColor(cls.id) }" />
            <span class="text-caption ml-1">{{ cls.name }}</span>
          </div>
        </div>
      </v-card-text>
    </v-card>
  </v-container>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useWorkspaceStore } from '../../stores/workspace.js'

const store = useWorkspaceStore()
const selectedWorkspaceId = ref(store.workspaces[0]?.id ?? null)

const allMonths = [
  { value: '2026-03', label: 'March 2026', shortLabel: 'Mar' },
  { value: '2026-04', label: 'April 2026', shortLabel: 'Apr' },
  { value: '2026-05', label: 'May 2026', shortLabel: 'May' },
  { value: '2026-06', label: 'June 2026', shortLabel: 'Jun' },
]

const selectedMonth = ref('2026-06')

const workspaceItems = computed(() => store.workspaces.map(w => ({ title: w.name, value: w.id })))
const monthItems = computed(() => allMonths.map(m => ({ title: m.label, value: m.value })))
const selectedMonthLabel = computed(() => allMonths.find(m => m.value === selectedMonth.value)?.label ?? '')
const selectedWorkspace = computed(() => store.workspaces.find(w => w.id === selectedWorkspaceId.value))
const workspaceClasses = computed(() => store.getClassesForWorkspace(selectedWorkspaceId.value))

const classRows = computed(() =>
  workspaceClasses.value.map(cls => {
    const rows = store.getStatsForClass(cls.id, selectedMonth.value)
    return {
      cls,
      sessionCount: rows.reduce((a, b) => a + b.sessionCount, 0),
      totalMinutes: rows.reduce((a, b) => a + b.totalMinutes, 0),
      participantCount: rows.reduce((a, b) => a + b.participantCount, 0),
    }
  })
)

const totals = computed(() => ({
  sessionCount: classRows.value.reduce((a, b) => a + b.sessionCount, 0),
  totalMinutes: classRows.value.reduce((a, b) => a + b.totalMinutes, 0),
  participantCount: classRows.value.reduce((a, b) => a + b.participantCount, 0),
}))

const kpis = computed(() => [
  { label: 'Total Sessions', value: totals.value.sessionCount, icon: 'mdi-video-wireless', color: 'primary' },
  { label: 'Total Hours', value: formatHours(totals.value.totalMinutes), icon: 'mdi-clock-outline', color: 'success' },
  { label: 'Participants', value: totals.value.participantCount, icon: 'mdi-account-group', color: 'blue' },
  { label: 'Classes', value: workspaceClasses.value.length, icon: 'mdi-google-classroom', color: 'teal' },
])

function monthStat(classId, month) {
  const rows = store.getStatsForClass(classId, month)
  return {
    totalMinutes: rows.reduce((a, b) => a + b.totalMinutes, 0),
    sessionCount: rows.reduce((a, b) => a + b.sessionCount, 0),
  }
}

const COLORS = ['#1565C0','#00897B','#6A1B9A','#E65100','#1B5E20']
function barColor(classId) {
  const idx = workspaceClasses.value.findIndex(c => c.id === classId)
  return COLORS[idx % COLORS.length]
}

function barHeight(classId, month) {
  const val = monthStat(classId, month).totalMinutes
  const allVals = workspaceClasses.value.flatMap(c => allMonths.map(m => monthStat(c.id, m.value).totalMinutes))
  const max = Math.max(...allVals, 1)
  return Math.max(4, Math.round((val / max) * 80))
}

function formatHours(min) {
  const h = Math.floor(min / 60)
  const m = min % 60
  return `${h}h ${m}m`
}

function formatMins(min) {
  if (min >= 60) return formatHours(min)
  return `${min}m`
}
</script>

<style scoped>
.table-row:hover { background: rgba(0,0,0,0.02); }
.total-row { background: rgba(21,101,192,0.05); border-top: 2px solid rgba(21,101,192,0.15); }
.trend-chart { display: flex; flex-direction: column; gap: 4px; }
.trend-row { display: flex; align-items: flex-end; gap: 12px; }
.trend-label { min-width: 90px; font-size: 12px; color: #666; }
.trend-bars { display: flex; align-items: flex-end; gap: 8px; flex: 1; }
.trend-bar-col { display: flex; flex-direction: column; align-items: center; gap: 4px; flex: 1; }
.trend-bar { width: 100%; border-radius: 4px 4px 0 0; transition: height 0.4s ease; min-width: 24px; }
.trend-bar-label { font-size: 10px; color: #999; }
.legend-item { display: flex; align-items: center; }
.legend-dot { display: inline-block; width: 10px; height: 10px; border-radius: 50%; }
</style>
