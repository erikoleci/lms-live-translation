<template>
  <v-app :theme="uiStore.darkMode ? 'dark' : 'light'">
    <!-- Role nav bar (only shown on non-full-screen pages) -->
    <v-app-bar
      v-if="showAppBar"
      color="primary"
      elevation="0"
      border="b"
    >
      <template #prepend>
        <v-app-bar-nav-icon
          class="d-flex d-sm-none"
          @click="navOpen = !navOpen"
        />
      </template>

      <v-app-bar-title>
        <div class="d-flex align-center gap-2">
          <v-icon color="white" size="22">mdi-translate</v-icon>
          <span class="font-weight-bold text-white">LMS Live Translation</span>
        </div>
      </v-app-bar-title>

      <template #append>
        <div class="d-none d-sm-flex align-center gap-1 mr-2">
          <v-btn
            v-for="item in navItems"
            :key="item.to"
            :to="item.to"
            variant="text"
            color="white"
            :prepend-icon="item.icon"
            class="nav-btn"
            :class="{ 'nav-btn--active': isActive(item.to) }"
          >
            {{ item.label }}
          </v-btn>
        </div>
        <v-btn
          :icon="uiStore.darkMode ? 'mdi-weather-sunny' : 'mdi-weather-night'"
          variant="text"
          color="white"
          size="small"
          @click="uiStore.toggleDarkMode()"
        />
      </template>
    </v-app-bar>

    <!-- Mobile drawer -->
    <v-navigation-drawer v-model="navOpen" temporary>
      <v-list>
        <v-list-subheader>Navigation</v-list-subheader>
        <v-list-item
          v-for="item in navItems"
          :key="item.to"
          :to="item.to"
          :prepend-icon="item.icon"
          :title="item.label"
          rounded="lg"
          @click="navOpen = false"
        />
      </v-list>
    </v-navigation-drawer>

    <v-main>
      <router-view v-slot="{ Component }">
        <transition name="page-fade" mode="out-in">
          <component :is="Component" />
        </transition>
      </router-view>
    </v-main>
  </v-app>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRoute } from 'vue-router'
import { useUiStore } from './stores/ui'

const uiStore = useUiStore()
const route = useRoute()
const navOpen = ref(false)

const navItems = [
  { to: '/teacher', icon: 'mdi-human-male-board', label: 'Teacher' },
  { to: '/student/join', icon: 'mdi-school', label: 'Student' },
  { to: '/admin', icon: 'mdi-shield-account', label: 'Admin' },
]

const FULL_SCREEN_ROUTES = ['/teacher/session', '/student/session']

const showAppBar = computed(() =>
  !FULL_SCREEN_ROUTES.some(prefix => route.path.startsWith(prefix))
)

function isActive(to: string) {
  return route.path.startsWith(to)
}
</script>

<style>
:root {
  font-family: 'Inter', 'Roboto', sans-serif;
}

.page-fade-enter-from,
.page-fade-leave-to {
  opacity: 0;
  transform: translateY(6px);
}
.page-fade-enter-active,
.page-fade-leave-active {
  transition: all 0.18s ease;
}

.v-application {
  background: #F5F7FA !important;
}
.v-theme--dark .v-application {
  background: #12121F !important;
}

.nav-btn { opacity: 0.75; }
.nav-btn--active { opacity: 1; background: rgba(255,255,255,0.15) !important; border-radius: 8px; }

/* Global scrollbar */
::-webkit-scrollbar { width: 6px; height: 6px; }
::-webkit-scrollbar-track { background: transparent; }
::-webkit-scrollbar-thumb { background: rgba(0,0,0,0.15); border-radius: 3px; }
::-webkit-scrollbar-thumb:hover { background: rgba(0,0,0,0.25); }
</style>
