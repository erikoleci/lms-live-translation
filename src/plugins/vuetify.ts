import { createVuetify } from 'vuetify'
import 'vuetify/styles'
import '@mdi/font/css/materialdesignicons.css'

export default createVuetify({
  theme: {
    defaultTheme: 'light',
    themes: {
      light: {
        colors: {
          primary: '#1565C0',
          secondary: '#0288D1',
          accent: '#00ACC1',
          success: '#2E7D32',
          warning: '#F57F17',
          error: '#C62828',
          info: '#0277BD',
          surface: '#FFFFFF',
          background: '#F5F7FA',
        },
      },
      dark: {
        colors: {
          primary: '#42A5F5',
          secondary: '#29B6F6',
          accent: '#26C6DA',
          success: '#66BB6A',
          warning: '#FFA726',
          error: '#EF5350',
          info: '#29B6F6',
          surface: '#1E1E2E',
          background: '#12121F',
        },
      },
    },
  },
  defaults: {
    VBtn: {
      rounded: 'lg',
    },
    VCard: {
      rounded: 'xl',
    },
    VTextField: {
      variant: 'outlined',
      rounded: 'lg',
    },
    VSelect: {
      variant: 'outlined',
      rounded: 'lg',
    },
    VChip: {
      rounded: 'lg',
    },
  },
})
