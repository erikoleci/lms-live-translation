import { ref, onUnmounted } from 'vue'
import { wsUrl } from '../services/api.js'

// Maps our IT/EN/SQ codes to BCP-47 locales the Web Speech API expects.
const STT_LOCALE = { IT: 'it-IT', EN: 'en-US', SQ: 'sq-AL' }

/**
 * FREE testing path for speech-to-text: uses the browser's built-in
 * Web Speech API (Chrome/Edge only) instead of streaming raw audio to a
 * paid cloud STT provider. Recognized text is sent as JSON over
 * WS /ws/sessions/{sessionId}/teacher-text, which the backend's
 * TeacherTextSocket feeds into the exact same transcript/translation/
 * broadcast pipeline as real cloud STT would.
 *
 * No API key, no signup, works immediately — good enough to test the full
 * live-captioning flow end to end. Swap this out for real audio streaming
 * (see the backend README) once you're ready to pay for a cloud STT vendor.
 */
export function useBrowserStt(sessionId, sourceLanguage = 'IT') {
  const listening = ref(false)
  const error = ref(null)
  const supported = typeof window !== 'undefined'
    && !!(window.SpeechRecognition || window.webkitSpeechRecognition)

  let recognition = null
  let socket = null

  function connectSocket() {
    socket = new WebSocket(wsUrl(`/ws/sessions/${sessionId}/teacher-text`))
  }

  function send(text, isFinal, confidence) {
    if (socket?.readyState === WebSocket.OPEN) {
      socket.send(JSON.stringify({ text, isFinal, sourceLanguage, confidence }))
    }
  }

  function start() {
    error.value = null
    if (!supported) {
      error.value = 'Speech recognition is only available in Chrome/Edge right now.'
      return
    }
    connectSocket()

    const SpeechRecognitionImpl = window.SpeechRecognition || window.webkitSpeechRecognition
    recognition = new SpeechRecognitionImpl()
    recognition.lang = STT_LOCALE[sourceLanguage] || 'en-US'
    recognition.continuous = true
    recognition.interimResults = true

    recognition.onresult = (event) => {
      for (let i = event.resultIndex; i < event.results.length; i++) {
        const result = event.results[i]
        const text = result[0].transcript.trim()
        if (!text) continue
        send(text, result.isFinal, result[0].confidence)
      }
    }

    recognition.onerror = (event) => {
      if (event.error === 'no-speech') return // benign, keeps listening
      error.value = `Speech recognition error: ${event.error}`
    }

    // Chrome auto-stops recognition after a period of silence; restart
    // transparently while the teacher still has the mic toggled on.
    recognition.onend = () => {
      if (listening.value) recognition.start()
    }

    recognition.start()
    listening.value = true
  }

  function stop() {
    listening.value = false
    recognition?.stop()
    recognition = null
    socket?.close()
    socket = null
  }

  onUnmounted(stop)

  return { start, stop, listening, error, supported }
}
