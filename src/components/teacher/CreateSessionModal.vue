<template>
  <v-dialog v-model="model" max-width="600" persistent scrollable>
    <v-card rounded="xl">
      <v-card-title class="d-flex align-center gap-2 pt-6 px-6">
        <v-avatar color="primary" size="36">
          <v-icon color="white" size="20">mdi-plus</v-icon>
        </v-avatar>
        <span>New Live Session</span>
        <v-spacer />
        <v-btn icon="mdi-close" variant="text" @click="model = false" />
      </v-card-title>

      <v-card-text class="px-6">
        <v-form ref="formRef" @submit.prevent="submit">
          <v-text-field v-model="form.title" label="Session Title" placeholder="e.g. Introduction to Machine Learning" prepend-inner-icon="mdi-text" :rules="[r => !!r || 'Title is required']" class="mb-3" />

          <v-select v-model="form.courseId" label="Course" prepend-inner-icon="mdi-book-open" :items="courses" item-title="name" item-value="id" :rules="[r => !!r || 'Course is required']" class="mb-3" />

          <v-row class="mb-1">
            <v-col cols="6">
              <v-select v-model="form.sourceLanguage" label="Source Language" prepend-inner-icon="mdi-microphone" :items="languageItems" :rules="[r => !!r || 'Required']" />
            </v-col>
            <v-col cols="6">
              <v-select v-model="form.targetLanguages" label="Target Languages" prepend-inner-icon="mdi-translate" :items="targetLangItems" multiple chips :rules="[r => r.length > 0 || 'Select at least one']" />
            </v-col>
          </v-row>

          <v-select v-model="form.accessMode" label="Access Mode" prepend-inner-icon="mdi-shield-account" :items="accessModes" class="mb-3" />

          <v-text-field v-model.number="form.maxParticipants" label="Max Participants" type="number" prepend-inner-icon="mdi-account-group" min="1" max="1000" class="mb-3" />

          <v-divider class="mb-4" />

          <div class="d-flex flex-column gap-1">
            <v-switch v-model="form.recordingEnabled" label="Enable audio recording (server-side)" color="primary" density="compact" hide-details />
            <v-switch v-model="form.studentTranscriptDownloadEnabled" label="Allow students to download transcript" color="primary" density="compact" hide-details />
          </div>
        </v-form>
      </v-card-text>

      <v-divider />
      <v-card-actions class="px-6 py-4 gap-2">
        <v-spacer />
        <v-btn variant="text" @click="() => { model = false; resetForm() }">Cancel</v-btn>
        <v-btn color="primary" variant="flat" prepend-icon="mdi-check" :loading="saving" @click="submit">Create Session</v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useSessionStore } from '../../stores/session.js'

const model = defineModel()
const emit = defineEmits(['created'])
const sessionStore = useSessionStore()
const formRef = ref()
const saving = ref(false)

const form = ref({
  title: '',
  courseId: 'cs401',
  sourceLanguage: 'IT',
  targetLanguages: ['EN', 'SQ'],
  accessMode: 'OPEN',
  maxParticipants: 300,
  recordingEnabled: false,
  studentTranscriptDownloadEnabled: true,
})

const courses = [
  { id: 'cs401', name: 'Computer Science 401' },
  { id: 'math201', name: 'Mathematics 201' },
  { id: 'hist301', name: 'History 301' },
  { id: 'lang101', name: 'Languages 101' },
]

const languageItems = [
  { title: '🇮🇹 Italian', value: 'IT' },
  { title: '🇬🇧 English', value: 'EN' },
  { title: '🇦🇱 Albanian', value: 'SQ' },
]

const targetLangItems = computed(() => languageItems.filter(l => l.value !== form.value.sourceLanguage))

const accessModes = [
  { title: '🔓 Open — anyone with QR/link', value: 'OPEN' },
  { title: '🔒 Closed — authenticated users only', value: 'CLOSED' },
]

function resetForm() {
  form.value = {
    title: '',
    courseId: 'cs401',
    sourceLanguage: 'IT',
    targetLanguages: ['EN', 'SQ'],
    accessMode: 'OPEN',
    maxParticipants: 300,
    recordingEnabled: false,
    studentTranscriptDownloadEnabled: true,
  }
  formRef.value?.resetValidation()
}

async function submit() {
  const { valid } = await formRef.value.validate()
  if (!valid) return
  saving.value = true
  await new Promise(r => setTimeout(r, 600))
  const session = sessionStore.createSession(form.value)
  saving.value = false
  model.value = false
  resetForm()
  emit('created', session.id)
}
</script>
