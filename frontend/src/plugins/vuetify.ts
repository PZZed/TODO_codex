import 'vuetify/styles'
import { createVuetify } from 'vuetify'

export const vuetify = createVuetify({
  theme: {
    defaultTheme: 'todoTheme',
    themes: {
      todoTheme: {
        dark: false,
        colors: {
          primary: '#3B82F6',
          secondary: '#0F172A',
          background: '#F8FAFC',
          surface: '#FFFFFF',
          success: '#16A34A',
          warning: '#CA8A04',
          error: '#DC2626'
        }
      }
    }
  },
  defaults: {
    VBtn: {
      rounded: 'lg'
    },
    VCard: {
      rounded: 'lg',
      elevation: 1
    },
    VTextField: {
      variant: 'outlined',
      density: 'comfortable',
      hideDetails: 'auto'
    },
    VTextarea: {
      variant: 'outlined',
      density: 'comfortable',
      hideDetails: 'auto'
    },
    VSelect: {
      variant: 'outlined',
      density: 'comfortable',
      hideDetails: 'auto'
    }
  }
})
