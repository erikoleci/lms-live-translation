import { ref, onUnmounted } from 'vue'
import { useSessionStore } from '../stores/session.js'

export function useAudio() {
  const sessionStore = useSessionStore()
  const stream = ref(null)
  const analyser = ref(null)
  const audioCtx = ref(null)
  const animFrame = ref(0)
  const error = ref(null)
  const supported = typeof navigator !== 'undefined' && !!navigator.mediaDevices

  async function requestMic() {
    error.value = null
    if (!supported) {
      error.value = 'Microphone not supported in this browser.'
      return false
    }
    try {
      stream.value = await navigator.mediaDevices.getUserMedia({ audio: true, video: false })
      audioCtx.value = new AudioContext()
      const source = audioCtx.value.createMediaStreamSource(stream.value)
      analyser.value = audioCtx.value.createAnalyser()
      analyser.value.fftSize = 256
      source.connect(analyser.value)
      sessionStore.setMicActive(true)
      startLevelMonitor()
      return true
    } catch (e) {
      error.value = e.name === 'NotAllowedError'
        ? 'Microphone permission denied. Please allow access in your browser settings.'
        : `Microphone error: ${e.message}`
      return false
    }
  }

  function startLevelMonitor() {
    if (!analyser.value) return
    const data = new Uint8Array(analyser.value.frequencyBinCount)
    const tick = () => {
      if (!analyser.value) return
      analyser.value.getByteFrequencyData(data)
      const avg = data.reduce((a, b) => a + b, 0) / data.length
      sessionStore.setAudioLevel(Math.min(100, (avg / 128) * 100))
      animFrame.value = requestAnimationFrame(tick)
    }
    animFrame.value = requestAnimationFrame(tick)
  }

  function stopMic() {
    cancelAnimationFrame(animFrame.value)
    stream.value?.getTracks().forEach(t => t.stop())
    audioCtx.value?.close()
    stream.value = null
    analyser.value = null
    audioCtx.value = null
    sessionStore.setMicActive(false)
    sessionStore.setAudioLevel(0)
  }

  onUnmounted(stopMic)

  return { requestMic, stopMic, error, supported }
}
