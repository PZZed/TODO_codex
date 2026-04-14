<template>
  <v-card class="mb-2" :aria-label="`Liste ${list.name}`">
    <v-card-text class="d-flex align-center justify-space-between ga-2">
      <v-btn
        variant="text"
        class="list-link justify-start"
        :aria-label="`Ouvrir la liste ${list.name}`"
        @click="$emit('open', list.id)"
      >
        <span class="font-weight-medium">{{ list.name }}</span>
      </v-btn>

      <div class="d-flex ga-1">
        <v-btn icon="mdi-pencil" variant="text" size="small" aria-label="Renommer la liste" @click="$emit('rename', list)" />
        <v-btn icon="mdi-delete" variant="text" size="small" color="error" aria-label="Supprimer la liste" @click="$emit('delete', list.id)" />
      </div>
    </v-card-text>
  </v-card>
</template>

<script setup lang="ts">
import type { TaskList } from '../../types/models'

defineProps<{ list: TaskList }>()
defineEmits<{ open: [id: string]; rename: [list: TaskList]; delete: [id: string] }>()
</script>

<style scoped>
.list-link {
  width: 100%;
  text-transform: none;
}
</style>
