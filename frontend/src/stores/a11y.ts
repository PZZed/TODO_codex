import { defineStore } from 'pinia'

export const useA11yStore = defineStore('a11y', {
  state: () => ({
    liveMessage: ''
  }),
  actions: {
    announce(message: string) {
      this.liveMessage = ''
      requestAnimationFrame(() => {
        this.liveMessage = message
      })
    }
  }
})
