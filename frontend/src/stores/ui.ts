import { defineStore } from 'pinia'

export const useUiStore = defineStore('ui', {
  state: () => ({
    globalError: '',
    snackbar: {
      show: false,
      message: ''
    }
  }),
  actions: {
    setGlobalError(message: string) {
      this.globalError = message
    },
    showSnackbar(message: string) {
      this.snackbar = { show: true, message }
    },
    hideSnackbar() {
      this.snackbar.show = false
    }
  }
})
