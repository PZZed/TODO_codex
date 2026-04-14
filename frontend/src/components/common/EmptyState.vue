<template>
  <v-card variant="tonal" class="pa-6 text-center">
    <div class="text-subtitle-1">{{ title }}</div>
    <div class="text-body-2 text-medium-emphasis">{{ subtitle }}</div>
  </v-card>
</template>

<script setup lang="ts">
defineProps<{ title: string; subtitle: string }>()
</script>
