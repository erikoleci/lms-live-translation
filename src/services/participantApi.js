import { api } from './api.js'

/** Maps to ParticipantResource (spec 6.1 "Join / Access"). */
export const participantApi = {
  join(sessionId, payload) {
    // payload: { targetLanguage, anonymousName, audioEnabled, voiceCode }
    return api.post(`/api/sessions/${sessionId}/participants`, payload).then(r => r.data)
  },

  list(sessionId) {
    return api.get(`/api/sessions/${sessionId}/participants`).then(r => r.data)
  },

  get(sessionId, participantId) {
    return api.get(`/api/sessions/${sessionId}/participants/${participantId}`).then(r => r.data)
  },

  updatePreferences(sessionId, participantId, patch) {
    // patch: { targetLanguage?, audioEnabled?, voiceCode? }
    return api.patch(`/api/sessions/${sessionId}/participants/${participantId}`, patch).then(r => r.data)
  },

  leave(sessionId, participantId) {
    return api.delete(`/api/sessions/${sessionId}/participants/${participantId}`)
  },
}
