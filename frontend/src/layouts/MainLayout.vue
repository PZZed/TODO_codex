<template>
  <a href="#main-content" class="skip-link">Aller au contenu principal</a>

  <AppTopBar />

  <v-main id="main-content" tabindex="-1" role="main">
    <div class="mobile-container">
      <router-view />
    </div>
  </v-main>

  <BottomNav />

  <div class="sr-only" aria-live="polite" aria-atomic="true">{{ a11y.liveMessage }}</div>
</template>

<script setup lang="ts">
import AppTopBar from '../components/common/AppTopBar.vue'
import BottomNav from '../components/common/BottomNav.vue'
import { useA11yStore } from '../stores/a11y'

const a11y = useA11yStore()
</script>

<style scoped>
.skip-link {
  position: absolute;
  left: -9999px;
  top: 0;
  z-index: 1000;
  background: #111827;
  color: #ffffff;
  padding: 8px 12px;
  border-radius: 6px;
}

.skip-link:focus {
  left: 12px;
  top: 12px;
}

.sr-only {
  position: absolute;
  width: 1px;
  height: 1px;
  padding: 0;
  margin: -1px;
  overflow: hidden;
  clip: rect(0, 0, 0, 0);
  white-space: nowrap;
  border: 0;
}
</style>
