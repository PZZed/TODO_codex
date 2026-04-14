<template>
  <v-card class="mb-2" :aria-label="`Tâche ${task.title}`">
    <v-card-text class="py-3">
      <div class="d-flex align-start ga-2 justify-space-between">
        <div class="d-flex ga-2 flex-grow-1 align-start">
          <v-checkbox-btn
            :model-value="isDone"
            color="success"
            :aria-label="`Marquer ${task.title} comme terminée`"
            @update:model-value="$emit('complete', task.id)"
          />

          <div class="flex-grow-1">
            <div class="font-weight-medium" :class="{ 'text-decoration-line-through': isDone }">{{ task.title }}</div>
            <div v-if="task.description" class="text-body-2 text-medium-emphasis">{{ task.description }}</div>
            <div class="text-caption mt-1">
              <span v-if="task.dueAt">Échéance: {{ formatDate(task.dueAt) }}</span>
              <span v-else>Aucune échéance</span>
            </div>
          </div>
        </div>

        <div class="d-flex ga-1">
          <v-btn icon="mdi-pencil" size="small" variant="text" aria-label="Modifier la tâche" @click="$emit('edit', task)" />
          <v-btn icon="mdi-delete" size="small" variant="text" color="error" aria-label="Supprimer la tâche" @click="$emit('delete', task.id)" />
        </div>
      </div>
    </v-card-text>
  </v-card>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { TaskItem } from '../../types/models'

const props = defineProps<{ task: TaskItem }>()

defineEmits<{ complete: [id: string]; edit: [task: TaskItem]; delete: [id: string] }>()

const isDone = computed(() => props.task.status === 'DONE')

function formatDate(iso: string) {
  const date = new Date(iso)
  if (Number.isNaN(date.getTime())) {
    return iso
  }
  return new Intl.DateTimeFormat('fr-FR', {
    dateStyle: 'short',
    timeStyle: 'short'
  }).format(date)
}
</script>
