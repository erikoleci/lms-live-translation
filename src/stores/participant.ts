import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { Participant, Language } from '../types'

const MOCK_PARTICIPANTS: Participant[] = [
  { id: 'p1', sessionId: 'sess-001', anonymousName: 'Student A', targetLanguage: 'EN', audioEnabled: true, voiceCode: 'en-US-female-1', joinedAt: new Date(Date.now() - 1200000).toISOString(), connectionStatus: 'CONNECTED' },
  { id: 'p2', sessionId: 'sess-001', anonymousName: 'Student B', targetLanguage: 'SQ', audioEnabled: false, voiceCode: 'sq-AL-female-1', joinedAt: new Date(Date.now() - 900000).toISOString(), connectionStatus: 'CONNECTED' },
  { id: 'p3', sessionId: 'sess-001', anonymousName: 'Marco R.', targetLanguage: 'EN', audioEnabled: true, voiceCode: 'en-US-male-1', joinedAt: new Date(Date.now() - 600000).toISOString(), connectionStatus: 'CONNECTED' },
  { id: 'p4', sessionId: 'sess-001', anonymousName: 'Ana K.', targetLanguage: 'SQ', audioEnabled: true, voiceCode: 'sq-AL-female-1', joinedAt: new Date(Date.now() - 300000).toISOString(), connectionStatus: 'RECONNECTING' },
  { id: 'p5', sessionId: 'sess-001', anonymousName: 'Guest-7742', targetLanguage: 'IT', audioEnabled: false, voiceCode: 'it-IT-female-1', joinedAt: new Date(Date.now() - 120000).toISOString(), connectionStatus: 'CONNECTED' },
]

export const useParticipantStore = defineStore('participant', () => {
  const participants = ref<Participant[]>(MOCK_PARTICIPANTS)

  const currentParticipant = ref<Participant | null>(null)

  function getParticipantsForSession(sessionId: string) {
    return participants.value.filter(p => p.sessionId === sessionId && !p.leftAt)
  }

  function joinSession(sessionId: string, name: string, language: Language): Participant {
    const p: Participant = {
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

  function updatePreferences(participantId: string, prefs: Partial<Pick<Participant, 'targetLanguage' | 'audioEnabled' | 'voiceCode'>>) {
    const p = participants.value.find(x => x.id === participantId)
    if (p) Object.assign(p, prefs)
    if (currentParticipant.value?.id === participantId) Object.assign(currentParticipant.value, prefs)
  }

  function leaveSession(participantId: string) {
    const p = participants.value.find(x => x.id === participantId)
    if (p) p.leftAt = new Date().toISOString()
    if (currentParticipant.value?.id === participantId) currentParticipant.value = null
  }

  return {
    participants,
    currentParticipant,
    getParticipantsForSession,
    joinSession,
    updatePreferences,
    leaveSession,
  }
})
