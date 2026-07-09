import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useUiStore = defineStore('ui', () => {
  const darkMode = ref(false)
  const captionFontSize = ref(18)
  const navDrawer = ref(false)

  function toggleDarkMode() {
    darkMode.value = !darkMode.value
  }

  function setCaptionFontSize(size) {
    captionFontSize.value = Math.min(36, Math.max(12, size))
  }

  function toggleNav() {
    navDrawer.value = !navDrawer.value
  }

  return { darkMode, captionFontSize, navDrawer, toggleDarkMode, setCaptionFontSize, toggleNav }
}, { persist: { pick: ['darkMode', 'captionFontSize'] } })
