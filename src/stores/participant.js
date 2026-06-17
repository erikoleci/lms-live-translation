import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useParticipantStore = defineStore('participant', () => {
  const participants = ref([])
  const currentParticipant = ref(null)

  function getParticipantsForSession(sessionId) {
    return participants.value.filter(p => p.sessionId === sessionId && !p.leftAt)
  }

  function joinSession(sessionId, name, language) {
    const p = {
      id: `p-${Date.now()}`,
      sessionId,
      anonymousName: name || `Guest-${Math.floor(Math.random() * 9999)}`,
      targetLanguage: language,
      audioEnabled: false,
      voiceCode: `${language.toLowerCase()}-female-1`,
      joinedAt: new Date().toISOString(),
      connectionStatus: 'CONNECTED',
    }
    participants.value.push(p)
    currentParticipant.value = p
    return p
  }

  function updatePreferences(participantId, prefs) {
    const p = participants.value.find(x => x.id === participantId)
    if (p) Object.assign(p, prefs)
    if (currentParticipant.value?.id === participantId) Object.assign(currentParticipant.value, prefs)
  }

  function leaveSession(participantId) {
    const p = participants.value.find(x => x.id === participantId)
    if (p) p.leftAt = new Date().toISOString()
    if (currentParticipant.value?.id === participantId) currentParticipant.value = null
  }

  return { participants, currentParticipant, getParticipantsForSession, joinSession, updatePreferences, leaveSession }
})
