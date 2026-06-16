import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { Provider, UsageStat } from '../types'

const MOCK_PROVIDERS: Provider[] = [
  {
    id: 'prov-1',
    providerCode: 'openai-stt',
    name: 'OpenAI Realtime STT',
    providerType: 'STT',
    priority: 1,
    enabled: true,
    supportedLanguages: ['IT', 'EN', 'SQ'],
    costLimit: 50,
    timeoutMs: 5000,
    fallbackProviderCode: 'azure-stt',
  },
  {
    id: 'prov-2',
    providerCode: 'azure-stt',
    name: 'Azure Speech-to-Text',
    providerType: 'STT',
    priority: 2,
    enabled: true,
    supportedLanguages: ['IT', 'EN', 'SQ'],
    costLimit: 30,
    timeoutMs: 6000,
  },
  {
    id: 'prov-3',
    providerCode: 'openai-translation',
    name: 'OpenAI Translation',
    providerType: 'TRANSLATION',
    priority: 1,
    enabled: true,
    supportedLanguages: ['IT', 'EN', 'SQ'],
    costLimit: 20,
    timeoutMs: 3000,
    fallbackProviderCode: 'azure-translator',
  },
  {
    id: 'prov-4',
    providerCode: 'azure-translator',
    name: 'Azure Translator',
    providerType: 'TRANSLATION',
    priority: 2,
    enabled: false,
    supportedLanguages: ['IT', 'EN', 'SQ'],
    costLimit: 15,
    timeoutMs: 4000,
  },
  {
    id: 'prov-5',
    providerCode: 'openai-tts',
    name: 'OpenAI TTS',
    providerType: 'TTS',
    priority: 1,
    enabled: true,
    supportedLanguages: ['IT', 'EN', 'SQ'],
    supportedVoices: [
      { code: 'en-US-alloy', name: 'Alloy (EN)', language: 'EN', gender: 'NEUTRAL' },
      { code: 'en-US-echo', name: 'Echo (EN)', language: 'EN', gender: 'MALE' },
      { code: 'it-IT-nova', name: 'Nova (IT)', language: 'IT', gender: 'FEMALE' },
      { code: 'sq-AL-shimmer', name: 'Shimmer (SQ)', language: 'SQ', gender: 'FEMALE' },
    ],
    costLimit: 25,
    timeoutMs: 4000,
  },
  {
    id: 'prov-6',
    providerCode: 'azure-neural-tts',
    name: 'Azure Neural TTS',
    providerType: 'TTS',
    priority: 2,
    enabled: true,
    supportedLanguages: ['IT', 'EN'],
    supportedVoices: [
      { code: 'en-US-JennyNeural', name: 'Jenny (EN)', language: 'EN', gender: 'FEMALE' },
      { code: 'it-IT-ElsaNeural', name: 'Elsa (IT)', language: 'IT', gender: 'FEMALE' },
    ],
    costLimit: 20,
    timeoutMs: 5000,
  },
]

function generateUsageStats(): UsageStat[] {
  const stats: UsageStat[] = []
  for (let i = 6; i >= 0; i--) {
    const d = new Date()
    d.setDate(d.getDate() - i)
    stats.push({
      date: d.toISOString().split('T')[0],
      sttMinutes: Math.floor(Math.random() * 120 + 20),
      translationChars: Math.floor(Math.random() * 50000 + 5000),
      ttsChars: Math.floor(Math.random() * 20000 + 2000),
      sessionCount: Math.floor(Math.random() * 8 + 1),
      estimatedCost: parseFloat((Math.random() * 5 + 0.5).toFixed(2)),
    })
  }
  return stats
}

export const useProviderStore = defineStore('provider', () => {
  const providers = ref<Provider[]>(MOCK_PROVIDERS)
  const usageStats = ref<UsageStat[]>(generateUsageStats())

  function toggleProvider(id: string) {
    const p = providers.value.find(x => x.id === id)
    if (p) p.enabled = !p.enabled
  }

  function updateProvider(id: string, updates: Partial<Provider>) {
    const p = providers.value.find(x => x.id === id)
    if (p) Object.assign(p, updates)
  }

  function getByType(type: Provider['providerType']) {
    return providers.value.filter(p => p.providerType === type)
  }

  return {
    providers,
    usageStats,
    toggleProvider,
    updateProvider,
    getByType,
  }
})
