import { api } from './api.js'

/** Maps to SessionResource on the backend (spec 6.1). */
export const sessionApi = {
  create(payload) {
    // payload: { title, courseId, sourceLanguage, targetLanguages, accessMode,
    //            recordingEnabled, studentTranscriptDownloadEnabled, maxParticipants }
    return api.post('/api/sessions', payload).then(r => r.data)
  },

  list(params = {}) {
    return api.get('/api/sessions', { params }).then(r => r.data)
  },

  get(sessionId) {
    return api.get(`/api/sessions/${sessionId}`).then(r => r.data)
  },

  update(sessionId, patch) {
    return api.patch(`/api/sessions/${sessionId}`, patch).then(r => r.data)
  },

  remove(sessionId) {
    return api.delete(`/api/sessions/${sessionId}`)
  },

  /** state: CREATED | WAITING | ACTIVE | PAUSED | ENDED | EXPIRED | FAILED */
  changeState(sessionId, state) {
    return api.patch(`/api/sessions/${sessionId}/state`, { state }).then(r => r.data)
  },

  joinInfo(sessionId) {
    return api.get(`/api/sessions/${sessionId}/join-info`).then(r => r.data)
  },

  /** Used by the student "enter code" screen, before the session UUID is known. */
  joinInfoByCode(joinCode) {
    return api.get(`/api/sessions/by-code/${joinCode}`).then(r => r.data)
  },

  qrImageUrl(sessionId) {
    return `${api.defaults.baseURL}/api/sessions/${sessionId}/qr-code`
  },

  qrPayload(sessionId) {
    return api.get(`/api/sessions/${sessionId}/qr-code/payload`).then(r => r.data)
  },

  transcript(sessionId) {
    return api.get(`/api/sessions/${sessionId}/transcript`).then(r => r.data)
  },

  translations(sessionId, language) {
    return api
      .get(`/api/sessions/${sessionId}/translations`, { params: language ? { language } : {} })
      .then(r => r.data)
  },
}
