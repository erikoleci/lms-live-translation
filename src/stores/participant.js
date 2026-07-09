import { defineStore } from 'pinia'
import { ref } from 'vue'
import { participantApi } from '../services/participantApi.js'

export const useParticipantStore = defineStore('participant', () => {
  const participants = ref([])
  const currentParticipant = ref(null)

  function getParticipantsForSession(sessionId) {
    return participants.value.filter(p => p.sessionId === sessionId && !p.leftAt)
  }

  /** Calls POST /api/sessions/{sessionId}/participants and stores the result. */
  async function joinSession(sessionId, name, language) {
    const p = await participantApi.join(sessionId, {
      targetLanguage: language,
      anonymousName: name || undefined,
      audioEnabled: false,
      voiceCode: `${language.toLowerCase()}-female-1`,
    })
    const normalized = {
      id: p.id,
      sessionId: p.sessionId,
      anonymousName: p.displayName,
      targetLanguage: p.targetLanguage,
      audioEnabled: p.audioEnabled,
      voiceCode: p.voiceCode,
      joinedAt: p.joinedAt,
      connectionStatus: p.connectionStatus,
    }
    participants.value.push(normalized)
    currentParticipant.value = normalized
    return normalized
  }

  function updatePreferences(participantId, prefs) {
    const p = participants.value.find(x => x.id === participantId)
    if (p) Object.assign(p, prefs)
    if (currentParticipant.value?.id === participantId) Object.assign(currentParticipant.value, prefs)
  }

  /** Persists preference changes to the backend (see LiveCaptions.vue's watcher). */
  async function persistPreferences(sessionId, participantId, prefs) {
    await participantApi.updatePreferences(sessionId, participantId, prefs)
    updatePreferences(participantId, prefs)
  }

  async function leaveSession(sessionId, participantId) {
    try {
      await participantApi.leave(sessionId, participantId)
    } finally {
      const p = participants.value.find(x => x.id === participantId)
      if (p) p.leftAt = new Date().toISOString()
      if (currentParticipant.value?.id === participantId) currentParticipant.value = null
    }
  }

  return {
    participants, currentParticipant,
    getParticipantsForSession, joinSession, updatePreferences, persistPreferences, leaveSession,
  }
})
