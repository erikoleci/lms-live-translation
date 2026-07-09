<template>
  <v-card
    rounded="xl" border
    :style="!provider.enabled ? 'opacity:0.55' : ''"
    class="h-100"
  >
    <v-card-item>
      <template #prepend>
        <v-avatar :color="typeColor" size="40" rounded="lg">
          <v-icon color="white" size="20">{{ typeIcon }}</v-icon>
        </v-avatar>
      </template>
      <v-card-title class="text-body-1 font-weight-bold">{{ provider.name }}</v-card-title>
      <v-card-subtitle class="text-caption">{{ provider.providerCode }}</v-card-subtitle>
      <template #append>
        <v-switch
          :model-value="provider.enabled"
          color="success" hide-details density="compact"
          @update:model-value="emit('toggle', provider.id)"
        />
      </template>
    </v-card-item>

    <v-card-text class="pt-0">
      <div class="d-flex flex-wrap gap-2 mb-3">
        <LanguageBadge v-for="lang in provider.supportedLanguages" :key="lang" :lang="lang" />
      </div>
      <v-row class="text-caption text-medium-emphasis">
        <v-col cols="6"><v-icon size="12" class="mr-1">mdi-sort-numeric-ascending</v-icon>Priority: <strong>{{ provider.priority }}</strong></v-col>
        <v-col cols="6"><v-icon size="12" class="mr-1">mdi-timer-outline</v-icon>Timeout: <strong>{{ provider.timeoutMs / 1000 }}s</strong></v-col>
        <v-col v-if="provider.costLimit" cols="6"><v-icon size="12" class="mr-1">mdi-currency-usd</v-icon>Limit: <strong>${{ provider.costLimit }}/mo</strong></v-col>
        <v-col v-if="provider.fallbackProviderCode" cols="6"><v-icon size="12" class="mr-1">mdi-backup-restore</v-icon>Fallback: <strong>{{ provider.fallbackProviderCode }}</strong></v-col>
      </v-row>
    </v-card-text>

    <v-card-actions class="px-4 pb-4 pt-0">
      <v-chip size="x-small" :color="typeColor" variant="tonal" class="font-weight-bold">{{ provider.providerType }}</v-chip>
      <v-chip v-if="provider.priority === 1" size="x-small" color="amber" variant="tonal" prepend-icon="mdi-star" class="font-weight-bold ml-2">Primary</v-chip>
      <v-spacer />
      <v-btn size="small" variant="text" prepend-icon="mdi-pencil" @click="emit('edit', provider.id)">Edit</v-btn>
    </v-card-actions>
  </v-card>
</template>

<script setup>
import { computed } from 'vue'
import LanguageBadge from '../shared/LanguageBadge.vue'

const props = defineProps({ provider: Object })
const emit = defineEmits(['toggle', 'edit'])

const typeColor = computed(() => ({ STT: 'blue', TRANSLATION: 'purple', TTS: 'teal' }[props.provider.providerType] ?? 'grey'))
const typeIcon = computed(() => ({ STT: 'mdi-microphone-settings', TRANSLATION: 'mdi-translate', TTS: 'mdi-speaker-wireless' }[props.provider.providerType] ?? 'mdi-cog'))
</script>
