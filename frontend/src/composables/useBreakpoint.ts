import { computed } from 'vue'
import { useDisplay } from 'vuetify'

export function useBreakpoint() {
  const display = useDisplay()

  const isMobile = computed(() => display.smAndDown.value)
  const isTablet = computed(() => display.md.value)
  const isDesktop = computed(() => display.lgAndUp.value)

  return { isMobile, isTablet, isDesktop }
}
