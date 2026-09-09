import { defineStore } from 'pinia'
import { ref } from 'vue'

function uid() {
  return crypto.randomUUID ? crypto.randomUUID() : `${Date.now()}-${Math.random().toString(36).slice(2)}`
}

/** Pure client-side participant list -- no backend, purely for UI state. */
export const useParticipantStore = defineStore('participant', () => {
  const participants = ref([])
  const currentParticipant = ref(null)

  function getParticipantsForSession(sessionId) {
    return participants.value.filter(p => p.sessionId === sessionId && !p.leftAt)
  }

  function joinSession(sessionId, name, language) {
    const normalized = {
      id: uid(),
      sessionId,
      anonymousName: name || 'Anonymous',
      targetLanguage: language,
      audioEnabled: false,
      voiceCode: `${language.toLowerCase()}-female-1`,
      joinedAt: new Date().toISOString(),
      connectionStatus: 'CONNECTED',
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

  function leaveSession(sessionId, participantId) {
    const p = participants.value.find(x => x.id === participantId)
    if (p) p.leftAt = new Date().toISOString()
    if (currentParticipant.value?.id === participantId) currentParticipant.value = null
  }

  return {
    participants, currentParticipant,
    getParticipantsForSession, joinSession, updatePreferences, leaveSession,
  }
})
