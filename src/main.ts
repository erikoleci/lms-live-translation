import { createApp } from 'vue'
import { createPinia } from 'pinia'
import { createVuetify } from 'vuetify'
import { createRouter, createWebHashHistory } from 'vue-router'
import '@mdi/font/css/materialdesignicons.css'
import 'vuetify/styles'

import App from './App.vue'
import AdminView from './views/admin.vue'
import TeacherView from './views/teacher.vue'
import StudentView from './views/student.vue'

const router = createRouter({
  history: createWebHashHistory(),
  routes: [
    { path: '/', redirect: '/admin' },
    { path: '/admin', component: AdminView },
    { path: '/teacher', component: TeacherView },
    { path: '/student', component: StudentView },
  ],
})

const vuetify = createVuetify({
  theme: {
    defaultTheme: 'light',
  },
})

const pinia = createPinia()

const app = createApp(App)
app.use(pinia)
app.use(router)
app.use(vuetify)
app.mount('#app')
