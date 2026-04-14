import { ref } from 'vue'

export function useAsyncState<T>() {
  const loading = ref(false)
  const error = ref<string | null>(null)
  const data = ref<T | null>(null)

  async function run(factory: () => Promise<T>) {
    loading.value = true
    error.value = null
    try {
      data.value = await factory()
      return data.value
    } catch (e) {
      error.value = e instanceof Error ? e.message : 'Unexpected error'
      throw e
    } finally {
      loading.value = false
    }
  }

  return { loading, error, data, run }
}
