import { createApp } from 'vue'
import { createPinia } from 'pinia'
import vuetify from './plugins/vuetify.js'
import router from './router/index.js'
import App from './App.vue'

const pinia = createPinia()
const app = createApp(App)

app.use(pinia)
app.use(router)
app.use(vuetify)
app.mount('#app')
