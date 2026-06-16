<template>
  <v-container fluid class="pa-6">
    <div class="d-flex align-center mb-6">
      <v-btn icon="mdi-arrow-left" variant="text" size="small" to="/admin" class="mr-2" />
      <div>
        <h1 class="text-h5 font-weight-bold">Provider Configuration</h1>
        <p class="text-body-2 text-medium-emphasis">Manage STT, Translation, and TTS providers</p>
      </div>
      <v-spacer />
      <v-btn color="primary" variant="flat" prepend-icon="mdi-plus">Add Provider</v-btn>
    </div>

    <!-- Filter tabs -->
    <v-tabs v-model="activeType" color="primary" class="mb-6">
      <v-tab value="ALL">
        <v-icon start>mdi-view-grid</v-icon> All
        <v-chip size="x-small" class="ml-2" variant="tonal">{{ providerStore.providers.length }}</v-chip>
      </v-tab>
      <v-tab value="STT">
        <v-icon start>mdi-microphone-settings</v-icon> STT
        <v-chip size="x-small" class="ml-2" color="blue" variant="tonal">
          {{ providerStore.getByType('STT').length }}
        </v-chip>
      </v-tab>
      <v-tab value="TRANSLATION">
        <v-icon start>mdi-translate</v-icon> Translation
        <v-chip size="x-small" class="ml-2" color="purple" variant="tonal">
          {{ providerStore.getByType('TRANSLATION').length }}
        </v-chip>
      </v-tab>
      <v-tab value="TTS">
        <v-icon start>mdi-speaker-wireless</v-icon> TTS
        <v-chip size="x-small" class="ml-2" color="teal" variant="tonal">
          {{ providerStore.getByType('TTS').length }}
        </v-chip>
      </v-tab>
    </v-tabs>

    <!-- Provider grid -->
    <v-row>
      <v-col
        v-for="provider in filteredProviders"
        :key="provider.id"
        cols="12"
        sm="6"
        lg="4"
      >
        <ProviderCard
          :provider="provider"
          @toggle="providerStore.toggleProvider($event)"
          @edit="openEdit($event)"
        />
      </v-col>
    </v-row>

    <!-- Fallback chain visualization -->
    <v-card rounded="xl" elevation="0" border class="mt-6">
      <v-card-title class="text-body-1 font-weight-bold pt-4 px-4">
        <v-icon start color="primary">mdi-backup-restore</v-icon>
        Fallback Chain
      </v-card-title>
      <v-card-text class="px-4 pb-4">
        <div v-for="type in (['STT', 'TRANSLATION', 'TTS'] as const)" :key="type" class="fallback-chain mb-4">
          <p class="text-caption font-weight-bold text-uppercase text-medium-emphasis mb-2">{{ type }}</p>
          <div class="chain-row">
            <template v-for="(provider, i) in sortedByPriority(type)" :key="provider.id">
              <div class="chain-node" :class="{ 'chain-node--disabled': !provider.enabled }">
                <v-icon size="14" :color="provider.enabled ? typeColor(type) : 'grey'">
                  {{ typeIcon(type) }}
                </v-icon>
                <span class="text-caption ml-1">{{ provider.name }}</span>
                <v-chip size="x-small" :color="provider.priority === 1 ? 'amber' : 'grey'" variant="text">
                  P{{ provider.priority }}
                </v-chip>
              </div>
              <v-icon v-if="i < sortedByPriority(type).length - 1" size="14" color="grey">mdi-arrow-right</v-icon>
            </template>
          </div>
        </div>
      </v-card-text>
    </v-card>

    <!-- Edit dialog -->
    <v-dialog v-model="editDialog" max-width="500">
      <v-card rounded="xl" v-if="editProvider">
        <v-card-title class="pt-6 px-6">
          <v-icon start color="primary">mdi-pencil</v-icon>
          Edit Provider
        </v-card-title>
        <v-card-text class="px-6">
          <v-text-field label="Cost Limit ($/month)" type="number" v-model.number="editProvider.costLimit" class="mb-3" />
          <v-text-field label="Timeout (ms)" type="number" v-model.number="editProvider.timeoutMs" class="mb-3" />
          <v-text-field label="Priority" type="number" v-model.number="editProvider.priority" min="1" max="9" />
        </v-card-text>
        <v-card-actions class="px-6 pb-6 gap-2">
          <v-spacer />
          <v-btn variant="text" @click="editDialog = false">Cancel</v-btn>
          <v-btn color="primary" variant="flat" @click="saveEdit">Save</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
  </v-container>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useProviderStore } from '../../stores/provider'
import ProviderCard from '../../components/admin/ProviderCard.vue'
import type { Provider, ProviderType } from '../../types'

const providerStore = useProviderStore()
const activeType = ref<'ALL' | ProviderType>('ALL')
const editDialog = ref(false)
const editProvider = ref<Provider | null>(null)

const filteredProviders = computed(() =>
  activeType.value === 'ALL'
    ? providerStore.providers
    : providerStore.getByType(activeType.value as ProviderType)
)

function sortedByPriority(type: ProviderType) {
  return providerStore.getByType(type).slice().sort((a, b) => a.priority - b.priority)
}

function typeColor(type: ProviderType) {
  return { STT: 'blue', TRANSLATION: 'purple', TTS: 'teal' }[type]
}

function typeIcon(type: ProviderType) {
  return { STT: 'mdi-microphone-settings', TRANSLATION: 'mdi-translate', TTS: 'mdi-speaker-wireless' }[type]
}

function openEdit(id: string) {
  const p = providerStore.providers.find(x => x.id === id)
  if (p) {
    editProvider.value = { ...p }
    editDialog.value = true
  }
}

function saveEdit() {
  if (editProvider.value) {
    providerStore.updateProvider(editProvider.value.id, editProvider.value)
  }
  editDialog.value = false
}
</script>

<style scoped>
.fallback-chain { }
.chain-row {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}
.chain-node {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 6px 10px;
  border-radius: 8px;
  background: rgba(0,0,0,0.04);
  border: 1px solid rgba(0,0,0,0.08);
}
.chain-node--disabled { opacity: 0.4; }
</style>
