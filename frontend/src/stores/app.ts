import { defineStore } from 'pinia'

export const useAppStore = defineStore('app', {
  state: () => ({
    userId: '',
    loading: false
  }),
  actions: {
    setUserId(id: string) {
      this.userId = id
    },
    setLoading(value: boolean) {
      this.loading = value
    }
  }
})
