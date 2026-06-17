<template>
  <v-card variant="outlined" rounded="xl" class="text-center pa-4" max-width="280">
    <p class="text-caption text-medium-emphasis mb-2 text-uppercase font-weight-bold" style="letter-spacing:1px">Scan to Join</p>

    <v-sheet
      color="white"
      rounded="lg"
      elevation="2"
      class="d-inline-flex pa-2 mb-3"
    >
      <qrcode-vue :value="joinUrl" :size="180" level="H" :margin="2" />
    </v-sheet>

    <v-chip
      color="primary" variant="flat" size="large"
      class="font-weight-bold mb-3 d-flex justify-center"
      style="letter-spacing:4px; font-size:20px"
      prepend-icon="mdi-key-variant"
    >{{ joinCode }}</v-chip>

    <div class="d-flex gap-2 justify-center flex-wrap">
      <v-btn size="small" variant="tonal" color="primary" prepend-icon="mdi-content-copy" @click="copyLink">Copy Link</v-btn>
      <v-btn size="small" variant="tonal" color="secondary" prepend-icon="mdi-download" @click="downloadQr">Download QR</v-btn>
    </div>

    <v-snackbar v-model="copied" timeout="2000" color="success" location="bottom">
      <v-icon start>mdi-check</v-icon> Link copied!
    </v-snackbar>
  </v-card>
</template>

<script setup>
import { ref } from 'vue'
import QrcodeVue from 'qrcode.vue'

const props = defineProps({ joinCode: String, sessionId: String })
const copied = ref(false)
const joinUrl = `${window.location.origin}${window.location.pathname}#/student/join/${props.joinCode}`

async function copyLink() { await navigator.clipboard.writeText(joinUrl); copied.value = true }
function downloadQr() {
  const canvas = document.querySelector('.v-card canvas')
  if (!canvas) return
  const a = document.createElement('a'); a.download = `join-${props.joinCode}.png`; a.href = canvas.toDataURL(); a.click()
}
</script>
