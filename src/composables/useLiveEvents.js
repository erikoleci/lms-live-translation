import { onUnmounted } from 'vue'
import { useWebSocket } from './useWebSocket.js'
import { wsUrl } from '../services/api.js'
import { useSessionStore } from '../stores/session.js'
import { useParticipantStore } from '../stores/participant.js'

/**
 * Connects to the backend's student broadcast channel
 * (WS /ws/sessions/{sessionId}/student) and wires every event type from
 * spec 6.2 into the Pinia stores. Used by BOTH the teacher's live dashboard
 * (to show the caption feed as it's produced) and the student's caption view
 * — they're the same event stream, just rendered differently.
 *
 * Replaces useSimulatedTranscript.js once a real backend is running.
 */
export function useLiveEvents(sessionId) {
  const sessionStore = useSessionStore()
  const participantStore = useParticipantStore()
  const { status, connect, disconnect, on } = useWebSocket(wsUrl(`/ws/sessions/${sessionId}/student`))

  on('TRANSCRIPT_PARTIAL', (payload) => applyTranscript(payload, false))
  on('TRANSCRIPT_FINAL', (payload) => applyTranscript(payload, true))
  on('TRANSLATION_PARTIAL', (payload) => applyTranslation(payload, false))
  on('TRANSLATION_FINAL', (payload) => applyTranslation(payload, true))
  on('TTS_AUDIO_CHUNK_READY', (payload) => {
    // Surfaced for components that want to fetch/play the chunk; the chunk
    // itself is small metadata (ids + language + voice), not the audio bytes.
    // TODO: wire to an audio-queue player once TTS storage/streaming exists.
  })
  on('PARTICIPANT_JOINED', (payload) => {
    const session = sessionStore.getSession(sessionId)
    if (session && payload?.participantCount != null) session.participantCount = payload.participantCount
  })
  on('PARTICIPANT_LEFT', (payload) => {
    const session = sessionStore.getSession(sessionId)
    if (session && payload?.participantCount != null) session.participantCount = payload.participantCount
  })
  on('SESSION_STATUS_CHANGED', (payload) => {
    if (payload?.status) sessionStore.updateSessionStatus(sessionId, payload.status)
  })
  on('ERROR', (payload) => {
    // TODO: surface as a snackbar/toast; scope is 'TRANSLATION' | 'TTS' etc.
    console.warn('[live-session] provider error', payload)
  })

  function applyTranscript(payload, isFinal) {
    sessionStore.addTranscriptSegment({
      id: payload.segmentId,
      sessionId,
      sequenceNo: payload.sequenceNo,
      sourceLanguage: payload.sourceLanguage,
      originalText: payload.text,
      isFinal,
      confidence: payload.confidence ?? null,
      startOffsetMs: payload.startOffsetMs ?? null,
      endOffsetMs: payload.endOffsetMs ?? null,
      createdAt: new Date().toISOString(),
      translations: [],
    })
  }

  function applyTranslation(payload, isFinal) {
    const segment = sessionStore.transcript.find(s => s.id === payload.transcriptSegmentId)
    if (!segment) return // translation arrived before its transcript segment; safe to drop
    if (!segment.translations) segment.translations = []
    const existing = segment.translations.find(t => t.targetLanguage === payload.targetLanguage)
    const translation = {
      id: payload.translationId,
      transcriptSegmentId: payload.transcriptSegmentId,
      targetLanguage: payload.targetLanguage,
      translatedText: payload.text,
      isFinal,
      createdAt: new Date().toISOString(),
    }
    if (existing) Object.assign(existing, translation)
    else segment.translations.push(translation)
  }

  onUnmounted(disconnect)

  return { status, connect, disconnect }
}
