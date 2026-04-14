<template>
  <v-card class="mb-3">
    <v-card-text>
      <div class="d-flex justify-space-between align-center">
        <div>
          <div class="font-weight-medium">{{ task.title }}</div>
          <div class="text-caption text-medium-emphasis">{{ task.status }}</div>
        </div>
        <v-chip size="small" color="primary" variant="tonal">{{ task.dueAt || 'Sans échéance' }}</v-chip>
      </div>
    </v-card-text>
  </v-card>
</template>

<script setup lang="ts">
import type { TaskItem } from '../../types/models'

defineProps<{ task: TaskItem }>()
</script>
