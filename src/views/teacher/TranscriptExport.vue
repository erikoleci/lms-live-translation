<template>
  <v-container class="pa-6" max-width="900">
    <div class="d-flex align-center mb-6">
      <v-btn icon="mdi-arrow-left" variant="text" size="small" to="/teacher" class="mr-2" />
      <div>
        <h1 class="text-h6 font-weight-bold">Session Transcript</h1>
        <p class="text-body-2 text-medium-emphasis" v-if="session">{{ session.title }}</p>
      </div>
      <v-spacer />
      <div class="d-flex gap-2">
        <v-btn variant="tonal" prepend-icon="mdi-download" @click="exportTxt">Export .txt</v-btn>
        <v-btn color="primary" variant="flat" prepend-icon="mdi-file-pdf-box" @click="exportPdf">Export PDF</v-btn>
      </div>
    </div>

    <div v-if="!session" class="text-center py-12">
      <v-icon size="64" color="grey-lighten-2">mdi-file-search</v-icon>
      <p class="text-h6 mt-4 text-medium-emphasis">Session not found</p>
    </div>

    <template v-else>
      <v-card rounded="xl" elevation="0" border class="mb-4">
        <v-card-text class="pa-4">
          <v-row>
            <v-col cols="6" sm="3"><p class="text-caption text-medium-emphasis">Status</p><StatusChip :status="session.status" /></v-col>
            <v-col cols="6" sm="3"><p class="text-caption text-medium-emphasis">Source</p><LanguageBadge :lang="session.sourceLanguage" /></v-col>
            <v-col cols="6" sm="3"><p class="text-caption text-medium-emphasis">Duration</p><p class="text-body-2 font-weight-bold">{{ duration }}</p></v-col>
            <v-col cols="6" sm="3"><p class="text-caption text-medium-emphasis">Segments</p><p class="text-body-2 font-weight-bold">{{ segments.length }}</p></v-col>
          </v-row>
        </v-card-text>
      </v-card>

      <v-tabs v-model="activeTab" color="primary" class="mb-4">
        <v-tab value="original"><LanguageBadge :lang="session.sourceLanguage" class="mr-2" /> Original</v-tab>
        <v-tab v-for="lang in session.targetLanguages" :key="lang" :value="lang">
          <LanguageBadge :lang="lang" class="mr-2" /> {{ langLabel(lang) }}
        </v-tab>
        <v-tab value="all"><v-icon start>mdi-view-list</v-icon> All</v-tab>
      </v-tabs>

      <div class="transcript-list">
        <div v-for="seg in segments" :key="seg.id" class="transcript-entry">
          <div class="entry-meta">
            <span class="text-caption text-disabled font-mono">{{ formatOffset(seg.startOffsetMs) }}</span>
            <v-chip size="x-small" variant="text" v-if="seg.confidence">{{ Math.round((seg.confidence ?? 0) * 100) }}%</v-chip>
          </div>
          <template v-if="activeTab === 'original' || activeTab === 'all'">
            <p class="entry-text">{{ seg.originalText }}</p>
          </template>
          <template v-if="activeTab !== 'original'">
            <div v-for="tr in filteredTranslations(seg)" :key="tr.id">
              <div class="d-flex align-center gap-1 mb-1"><LanguageBadge :lang="tr.targetLanguage" /></div>
              <p class="entry-text entry-text--translated">{{ tr.translatedText }}</p>
            </div>
          </template>
        </div>
      </div>

      <div v-if="!segments.length" class="text-center py-12">
        <v-icon size="48" color="grey-lighten-2">mdi-text-search</v-icon>
        <p class="text-body-2 text-disabled mt-3">No transcript segments recorded.</p>
      </div>
    </template>

    <v-snackbar v-model="snack.show" :color="snack.color" timeout="3000" location="bottom">{{ snack.text }}</v-snackbar>
  </v-container>
</template>

<script setup>
import { computed, ref } from 'vue'
import { useRoute } from 'vue-router'
import { useSessionStore } from '../../stores/session.js'
import StatusChip from '../../components/shared/StatusChip.vue'
import LanguageBadge from '../../components/shared/LanguageBadge.vue'

const route = useRoute()
const sessionStore = useSessionStore()
const sessionId = route.params.id
const session = computed(() => sessionStore.getSession(sessionId))
const segments = computed(() => sessionStore.getTranscript(sessionId))
const activeTab = ref('original')
const snack = ref({ show: false, text: '', color: 'success' })

const duration = computed(() => {
  if (!session.value?.startedAt) return '—'
  const end = session.value.endedAt ? new Date(session.value.endedAt) : new Date()
  const ms = end.getTime() - new Date(session.value.startedAt).getTime()
  return `${Math.floor(ms / 60000)}m ${Math.floor((ms % 60000) / 1000)}s`
})

const langLabels = { IT: 'Italian', EN: 'English', SQ: 'Albanian' }

function langLabel(lang) { return langLabels[lang] }

function formatOffset(ms) {
  const s = Math.floor(ms / 1000)
  return `${String(Math.floor(s / 60)).padStart(2, '0')}:${String(s % 60).padStart(2, '0')}`
}

function filteredTranslations(seg) {
  if (!seg.translations) return []
  if (activeTab.value === 'all') return seg.translations
  return seg.translations.filter(t => t.targetLanguage === activeTab.value)
}

function exportTxt() {
  const lines = segments.value.map(seg => `[${formatOffset(seg.startOffsetMs)}] ${seg.originalText}`).join('\n')
  const blob = new Blob([lines], { type: 'text/plain' })
  const a = document.createElement('a')
  a.href = URL.createObjectURL(blob)
  a.download = `transcript-${sessionId}.txt`
  a.click()
  snack.value = { show: true, text: 'Transcript exported as .txt', color: 'success' }
}

function exportPdf() {
  snack.value = { show: true, text: 'PDF export requires backend integration', color: 'info' }
}
</script>

<style scoped>
.transcript-list { display: flex; flex-direction: column; gap: 2px; }
.transcript-entry { padding: 12px 16px; border-radius: 10px; border: 1px solid rgba(0,0,0,0.06); background: white; }
.transcript-entry:hover { background: #f9f9fb; }
.entry-meta { display: flex; align-items: center; gap: 8px; margin-bottom: 4px; }
.entry-text { font-size: 15px; line-height: 1.6; margin: 0; }
.entry-text--translated { color: #555; font-style: italic; margin-bottom: 8px; }
.font-mono { font-family: 'Courier New', monospace; }
</style>
