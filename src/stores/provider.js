import { defineStore } from 'pinia'
import { ref } from 'vue'
import { providerApi } from '../services/providerApi.js'

export const useProviderStore = defineStore('provider', () => {
  const providers = ref([])
  const usageStats = ref([]) // TODO: no usage-stats endpoint yet on the backend; wire once /api/admin/usage exists
  const loading = ref(false)

  async function fetchProviders() {
    loading.value = true
    try {
      providers.value = await providerApi.list()
      return providers.value
    } finally {
      loading.value = false
    }
  }

  async function toggleProvider(id) {
    const p = providers.value.find(x => x.id === id)
    if (!p) return
    const updated = await providerApi.update(id, { ...toRequestPayload(p), enabled: !p.enabled })
    Object.assign(p, updated)
  }

  async function updateProvider(id, updates) {
    const p = providers.value.find(x => x.id === id)
    if (!p) return
    const updated = await providerApi.update(id, toRequestPayload({ ...p, ...updates }))
    Object.assign(p, updated)
  }

  function getByType(type) {
    return providers.value.filter(p => p.providerType === type)
  }

  /** Maps the flat UI shape back to ProviderConfigRequest expected by the backend. */
  function toRequestPayload(p) {
    return {
      providerCode: p.providerCode,
      providerType: p.providerType,
      priority: p.priority,
      enabled: p.enabled,
      credentialsRef: p.credentialsRef,
      supportedLanguages: p.supportedLanguages,
      supportedVoices: p.supportedVoices,
      costLimitCents: p.costLimitCents ?? (p.costLimit != null ? Math.round(p.costLimit * 100) : null),
      timeoutMs: p.timeoutMs,
      fallbackProviderCode: p.fallbackProviderCode,
    }
  }

  return { providers, usageStats, loading, fetchProviders, toggleProvider, updateProvider, getByType }
})
