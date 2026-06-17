import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useProviderStore = defineStore('provider', () => {
  const providers = ref([])
  const usageStats = ref([])

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
