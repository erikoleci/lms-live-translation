import { ref } from 'vue'

// Maps our IT/EN/SQ codes to BCP-47 locales for voice matching.
const TTS_LOCALE = { IT: 'it-IT', EN: 'en-US', SQ: 'sq-AL' }

/**
 * FREE testing path for text-to-speech: uses the browser's built-in
 * SpeechSynthesis API instead of a paid cloud TTS provider. No backend
 * involvement at all — call speak() whenever a TRANSLATION_FINAL event
 * arrives for the student's own selected language (see LiveCaptions.vue).
 *
 * Voice quality/availability depends on the OS (Windows/Mac ship decent
 * multilingual voices; Albanian support varies by platform — if no sq-AL
 * voice is installed, the browser falls back to its default voice, which
 * will mispronounce Albanian text but still confirms the pipeline works).
 */
export function useBrowserTts() {
  const supported = typeof window !== 'undefined' && !!window.speechSynthesis
  const speaking = ref(false)

  function pickVoice(langCode) {
    if (!supported) return null
    const locale = TTS_LOCALE[langCode] || langCode
    const voices = window.speechSynthesis.getVoices()
    return voices.find(v => v.lang === locale)
      || voices.find(v => v.lang?.startsWith(locale.split('-')[0]))
      || null
  }

  function speak(text, langCode) {
    if (!supported || !text) return
    window.speechSynthesis.cancel() // don't overlap with a previous still-speaking utterance
    const utterance = new SpeechSynthesisUtterance(text)
    const voice = pickVoice(langCode)
    if (voice) utterance.voice = voice
    utterance.lang = TTS_LOCALE[langCode] || langCode
    utterance.onstart = () => { speaking.value = true }
    utterance.onend = () => { speaking.value = false }
    window.speechSynthesis.speak(utterance)
  }

  function stop() {
    window.speechSynthesis?.cancel()
    speaking.value = false
  }

  return { speak, stop, speaking, supported }
}
