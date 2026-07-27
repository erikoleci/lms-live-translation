import { defineStore } from 'pinia'
import { ref } from 'vue'

const DEMO_PROVIDERS = [
  { id: 'p1', providerCode: 'MYMEMORY_FREE', providerType: 'TRANSLATION', priority: 10, enabled: true, supportedLanguages: 'IT,EN,SQ', timeoutMs: 8000 },
  { id: 'p2', providerCode: 'AZURE_SPEECH', providerType: 'STT', priority: 20, enabled: false, supportedLanguages: 'IT,EN,SQ,FR,DE', timeoutMs: 5000 },
  { id: 'p3', providerCode: 'OPENAI_TTS', providerType: 'TTS', priority: 20, enabled: false, supportedLanguages: 'EN,IT', timeoutMs: 5000 },
]

const DEMO_USAGE_STATS = [
  { label: 'Mon', minutes: 42 }, { label: 'Tue', minutes: 68 }, { label: 'Wed', minutes: 35 },
  { label: 'Thu', minutes: 90 }, { label: 'Fri', minutes: 54 }, { label: 'Sat', minutes: 12 }, { label: 'Sun', minutes: 8 },
]

/** Pure client-side demo data for the admin provider-config screens. */
export const useProviderStore = defineStore('provider', () => {
  const providers = ref(DEMO_PROVIDERS.map(p => ({ ...p })))
  const usageStats = ref(DEMO_USAGE_STATS)

  function toggleProvider(id) {
    const p = providers.value.find(x => x.id === id)
    if (p) p.enabled = !p.enabled
  }

  function updateProvider(id, updates) {
    const p = providers.value.find(x => x.id === id)
    if (p) Object.assign(p, updates)
  }

  function getByType(type) {
    return providers.value.filter(p => p.providerType === type)
  }

  return { providers, usageStats, toggleProvider, updateProvider, getByType }
})
