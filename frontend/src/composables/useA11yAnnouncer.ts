import { ref } from 'vue'

export function useA11yAnnouncer() {
  const message = ref('')

  function announce(text: string) {
    message.value = ''
    requestAnimationFrame(() => {
      message.value = text
    })
  }

  return { message, announce }
}
