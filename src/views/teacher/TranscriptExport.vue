<template>
  <v-container class="pa-3 pa-sm-4 pa-md-6" max-width="900">
    <div class="d-flex align-center flex-wrap gap-3 mb-4 mb-sm-6">
      <v-btn icon="mdi-arrow-left" variant="text" size="small" to="/teacher" class="mr-1" />
      <div class="flex-grow-1">
        <h1 class="text-h6 font-weight-bold">Session Transcript</h1>
        <p v-if="session" class="text-body-2 text-medium-emphasis">{{ session.title }}</p>
      </div>
      <div class="d-flex gap-2 flex-wrap">
        <v-btn variant="tonal" prepend-icon="mdi-download" rounded="lg" size="small" @click="exportTxt">
          <span class="d-none d-sm-inline">Export </span>.txt
        </v-btn>
        <v-btn color="primary" variant="flat" prepend-icon="mdi-file-pdf-box" rounded="lg" size="small" @click="exportPdf">
          <span class="d-none d-sm-inline">Export </span>PDF
        </v-btn>
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

      <div class="d-flex flex-column gap-1">
        <v-sheet
          v-for="seg in segments" :key="seg.id"
          rounded="lg" border
          class="pa-3 pa-sm-4"
        >
          <div class="d-flex align-center gap-2 mb-1">
            <span class="text-caption text-disabled font-weight-medium" style="font-family:'Courier New',monospace">{{ formatOffset(seg.startOffsetMs) }}</span>
            <v-chip v-if="seg.confidence" size="x-small" variant="text">{{ Math.round((seg.confidence ?? 0) * 100) }}%</v-chip>
          </div>
          <template v-if="activeTab === 'original' || activeTab === 'all'">
            <p class="text-body-2 font-weight-medium ma-0 mb-1" style="font-size:15px; line-height:1.6">{{ seg.originalText }}</p>
          </template>
          <template v-if="activeTab !== 'original'">
            <div v-for="tr in filteredTranslations(seg)" :key="tr.id">
              <div class="d-flex align-center gap-1 mb-1"><LanguageBadge :lang="tr.targetLanguage" /></div>
              <p class="text-body-2 text-medium-emphasis font-italic ma-0 mb-2" style="font-size:14px">{{ tr.translatedText }}</p>
            </div>
          </template>
        </v-sheet>
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
  const a = document.createElement('a')
  a.href = URL.createObjectURL(new Blob([lines], { type: 'text/plain' }))
  a.download = `transcript-${sessionId}.txt`; a.click()
  snack.value = { show: true, text: 'Transcript exported as .txt', color: 'success' }
}
function exportPdf() { snack.value = { show: true, text: 'PDF export requires backend integration', color: 'info' } }
</script>
