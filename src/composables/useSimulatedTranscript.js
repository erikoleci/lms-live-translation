import { onUnmounted } from 'vue'
import { useSessionStore } from '../stores/session.js'

const SENTENCES_IT = [
  "Il machine learning è una branca dell'intelligenza artificiale.",
  'Le reti neurali si ispirano al funzionamento del cervello umano.',
  "L'addestramento di un modello richiede grandi quantità di dati.",
  "La backpropagation è l'algoritmo fondamentale per l'apprendimento.",
  'I modelli trasformer hanno rivoluzionato il natural language processing.',
  "La regolarizzazione evita l'overfitting nei modelli complessi.",
  'Il gradient descent ottimizza la funzione di loss iterativamente.',
]

const TRANSLATIONS_EN = {
  "Il machine learning è una branca dell'intelligenza artificiale.": 'Machine learning is a branch of artificial intelligence.',
  'Le reti neurali si ispirano al funzionamento del cervello umano.': 'Neural networks are inspired by the functioning of the human brain.',
  "L'addestramento di un modello richiede grandi quantità di dati.": 'Training a model requires large amounts of data.',
  "La backpropagation è l'algoritmo fondamentale per l'apprendimento.": 'Backpropagation is the fundamental algorithm for learning.',
  'I modelli trasformer hanno rivoluzionato il natural language processing.': 'Transformer models have revolutionized natural language processing.',
  "La regolarizzazione evita l'overfitting nei modelli complessi.": 'Regularization prevents overfitting in complex models.',
  'Il gradient descent ottimizza la funzione di loss iterativamente.': 'Gradient descent optimizes the loss function iteratively.',
}

const TRANSLATIONS_SQ = {
  "Il machine learning è una branca dell'intelligenza artificiale.": 'Mësimi automatik është një degë e inteligjencës artificiale.',
  'Le reti neurali si ispirano al funzionamento del cervello umano.': 'Rrjetet nervore frymëzohen nga funksionimi i trurit njerëzor.',
  "L'addestramento di un modello richiede grandi quantità di dati.": 'Trajnimi i modelit kërkon sasi të mëdha të dhënash.',
  "La backpropagation è l'algoritmo fondamentale per l'apprendimento.": 'Backpropagation është algoritmi themelor për të mësuar.',
  'I modelli trasformer hanno rivoluzionato il natural language processing.': 'Modelet transformer kanë revolucionizuar përpunimin e gjuhës natyrore.',
  "La regolarizzazione evita l'overfitting nei modelli complessi.": 'Rregullarizimi parandalon overfitting-un në modelet komplekse.',
  'Il gradient descent ottimizza la funzione di loss iterativamente.': 'Gradient descent optimizon funksionin e humbjeve në mënyrë iterative.',
}

export function useSimulatedTranscript(sessionId) {
  const store = useSessionStore()
  let seqNo = store.getTranscript(sessionId).length + 1
  let intervalId = null
  let sentenceIdx = 0

  function start() {
    if (intervalId) return
    const session = store.getSession(sessionId)
    const sourceLang = session?.sourceLanguage ?? 'IT'

    intervalId = setInterval(() => {
      const text = SENTENCES_IT[sentenceIdx % SENTENCES_IT.length]
      const now = Date.now()

      const translations = []
      const targetLangs = session?.targetLanguages ?? ['EN', 'SQ']
      for (const lang of targetLangs) {
        if (lang === sourceLang) continue
        const translated = lang === 'EN' ? (TRANSLATIONS_EN[text] ?? text)
          : lang === 'SQ' ? (TRANSLATIONS_SQ[text] ?? text)
          : text
        translations.push({ id: `tr-${lang.toLowerCase()}-${now}`, transcriptSegmentId: `seg-sim-${now}`, targetLanguage: lang, translatedText: translated, isFinal: true, createdAt: new Date().toISOString() })
      }

      const segment = {
        id: `seg-sim-${now}`,
        sessionId,
        sequenceNo: seqNo++,
        sourceLanguage: sourceLang,
        originalText: text,
        isFinal: true,
        confidence: parseFloat((0.92 + Math.random() * 0.07).toFixed(2)),
        startOffsetMs: (seqNo - 1) * 4000,
        endOffsetMs: seqNo * 4000,
        createdAt: new Date().toISOString(),
        translations,
      }
      store.addTranscriptSegment(segment)
      sentenceIdx++
    }, 5000)
  }

  function stop() {
    if (intervalId) { clearInterval(intervalId); intervalId = null }
  }

  onUnmounted(stop)

  return { start, stop }
}
