<template>
  <v-container fluid class="pa-3 pa-sm-4 pa-md-6">

    <div class="d-flex align-center flex-wrap gap-3 mb-4 mb-sm-6">
      <div class="flex-grow-1">
        <h1 class="text-h6 text-sm-h5 font-weight-bold">Admin Dashboard</h1>
        <p class="text-body-2 text-medium-emphasis">System overview and configuration</p>
      </div>
      <v-btn color="primary" variant="tonal" prepend-icon="mdi-cog" to="/admin/providers" rounded="lg">
        <span class="d-none d-sm-inline">Manage </span>Providers
      </v-btn>
    </div>

    <!-- KPI row -->
    <v-row class="mb-4 mb-sm-6">
      <v-col v-for="kpi in kpis" :key="kpi.label" cols="6" sm="3">
        <v-card rounded="xl" elevation="0" border class="pa-3 pa-sm-4">
          <div class="d-flex align-center justify-space-between mb-2">
            <v-avatar :color="`${kpi.color}-lighten-4`" size="36" rounded="lg">
              <v-icon :color="kpi.color" size="18">{{ kpi.icon }}</v-icon>
            </v-avatar>
            <v-chip size="x-small" :color="kpi.trend > 0 ? 'success' : 'error'" variant="tonal">
              {{ kpi.trend > 0 ? '+' : '' }}{{ kpi.trend }}%
            </v-chip>
          </div>
          <p class="text-h5 font-weight-bold mb-0">{{ kpi.value }}</p>
          <p class="text-caption text-medium-emphasis">{{ kpi.label }}</p>
        </v-card>
      </v-col>
    </v-row>

    <v-row>
      <!-- Usage chart -->
      <v-col cols="12" md="8">
        <UsageChart :stats="providerStore.usageStats" />
      </v-col>

      <!-- Provider health -->
      <v-col cols="12" md="4">
        <v-card rounded="xl" elevation="0" border height="100%">
          <v-card-title class="text-body-1 font-weight-bold pt-4 px-4">
            <v-icon start color="primary">mdi-server-network</v-icon>
            Provider Health
          </v-card-title>
          <v-card-text class="px-4 pb-2">
            <v-list density="compact" nav class="pa-0">
              <v-list-item
                v-for="p in providerStore.providers" :key="p.id"
                :border="true"
                rounded="lg"
                class="mb-1 px-2"
              >
                <template #prepend>
                  <v-icon size="14" :color="p.enabled ? 'success' : 'grey'" class="mr-2">
                    {{ p.enabled ? 'mdi-check-circle' : 'mdi-minus-circle' }}
                  </v-icon>
                </template>
                <v-list-item-title class="text-body-2">{{ p.name }}</v-list-item-title>
                <template #append>
                  <div class="d-flex align-center gap-1">
                    <v-chip size="x-small" :color="typeColor(p.providerType)" variant="tonal">{{ p.providerType }}</v-chip>
                    <span class="text-caption text-disabled">P{{ p.priority }}</span>
                  </div>
                </template>
              </v-list-item>
            </v-list>
          </v-card-text>
          <v-card-actions class="px-4 pb-4 pt-0">
            <v-btn variant="tonal" size="small" to="/admin/providers" prepend-icon="mdi-cog" block rounded="lg">Configure</v-btn>
          </v-card-actions>
        </v-card>
      </v-col>
    </v-row>

    <v-row class="mt-2">
      <!-- Languages -->
      <v-col cols="12" sm="6">
        <v-card rounded="xl" elevation="0" border>
          <v-card-title class="text-body-1 font-weight-bold pt-4 px-4">
            <v-icon start color="primary">mdi-translate</v-icon>
            Supported Languages
          </v-card-title>
          <v-card-text class="px-4 pb-3">
            <v-list density="compact" nav class="pa-0">
              <v-list-item
                v-for="lang in languages" :key="lang.code"
                class="px-0"
              >
                <template #prepend>
                  <span style="font-size:22px; line-height:1" class="mr-3">{{ lang.flag }}</span>
                </template>
                <v-list-item-title class="text-body-2 font-weight-bold">{{ lang.name }}</v-list-item-title>
                <v-list-item-subtitle class="text-caption">{{ lang.code }}</v-list-item-subtitle>
                <template #append>
                  <div class="d-flex gap-1">
                    <v-chip size="x-small" color="success" variant="tonal">STT</v-chip>
                    <v-chip size="x-small" color="purple" variant="tonal">TL</v-chip>
                    <v-chip size="x-small" color="teal" variant="tonal">TTS</v-chip>
                  </div>
                </template>
              </v-list-item>
            </v-list>
          </v-card-text>
        </v-card>
      </v-col>

      <!-- Policy -->
      <v-col cols="12" sm="6">
        <v-card rounded="xl" elevation="0" border>
          <v-card-title class="text-body-1 font-weight-bold pt-4 px-4">
            <v-icon start color="primary">mdi-shield-check</v-icon>
            Session Policy
          </v-card-title>
          <v-card-text class="px-4 pb-3">
            <v-list density="compact" nav class="pa-0">
              <v-list-item
                v-for="policy in policies" :key="policy.label"
                class="px-0"
              >
                <v-list-item-title class="text-body-2 font-weight-bold">{{ policy.label }}</v-list-item-title>
                <v-list-item-subtitle class="text-caption">{{ policy.description }}</v-list-item-subtitle>
                <template #append>
                  <v-chip size="small" color="primary" variant="tonal" class="font-weight-bold">{{ policy.value }}</v-chip>
                </template>
              </v-list-item>
            </v-list>
          </v-card-text>
        </v-card>
      </v-col>
    </v-row>

  </v-container>
</template>

<script setup>
import { useProviderStore } from '../../stores/provider.js'
import UsageChart from '../../components/admin/UsageChart.vue'

const providerStore = useProviderStore()

const kpis = [
  { label: 'Active Sessions', value: 2, icon: 'mdi-broadcast', color: 'success', trend: 14 },
  { label: 'Participants', value: 79, icon: 'mdi-account-group', color: 'blue', trend: 22 },
  { label: 'STT Minutes', value: '142', icon: 'mdi-microphone', color: 'purple', trend: -5 },
  { label: 'Cost Today', value: '$1.84', icon: 'mdi-currency-usd', color: 'orange', trend: 8 },
]
const languages = [
  { code: 'IT', name: 'Italian', flag: '🇮🇹' },
  { code: 'EN', name: 'English', flag: '🇬🇧' },
  { code: 'SQ', name: 'Albanian', flag: '🇦🇱' },
]
const policies = [
  { label: 'Max Participants', description: 'Default session limit', value: '300' },
  { label: 'Transcript Retention', description: 'Auto-delete after', value: '90 days' },
  { label: 'Audio Recording', description: 'Default setting', value: 'Disabled' },
  { label: 'Session Max Duration', description: 'Auto-expire after', value: '8 hours' },
  { label: 'Provider Fallback', description: 'On primary failure', value: 'Enabled' },
]
const typeColors = { STT: 'blue', TRANSLATION: 'purple', TTS: 'teal' }
function typeColor(t) { return typeColors[t] }
</script>
