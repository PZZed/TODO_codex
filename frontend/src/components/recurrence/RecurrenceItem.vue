<template>
  <v-card class="mb-2" :aria-label="`Récurrence ${item.task.title}`">
    <v-card-text class="py-3">
      <div class="d-flex align-start justify-space-between ga-2">
        <div>
          <div class="font-weight-medium">{{ item.task.title }}</div>
          <div v-if="item.task.description" class="text-body-2 text-medium-emphasis">{{ item.task.description }}</div>
          <div class="text-caption mt-1">{{ summary }}</div>
        </div>
        <div class="d-flex ga-1">
          <v-btn icon="mdi-pencil" size="small" variant="text" aria-label="Modifier la récurrence" @click="$emit('edit', item)" />
          <v-btn icon="mdi-delete" size="small" variant="text" color="error" aria-label="Supprimer la récurrence" @click="$emit('delete', item.task.id)" />
        </div>
      </div>
    </v-card-text>
  </v-card>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { recurrenceSummary } from '../../utils/recurrenceSummary'
import type { TaskItem } from '../../types/models'
import type { RecurrenceRule } from '../../services/api/recurrenceService'

export interface RecurrenceListItem {
  task: TaskItem
  recurrence: RecurrenceRule
}

const props = defineProps<{ item: RecurrenceListItem }>()

defineEmits<{ edit: [item: RecurrenceListItem]; delete: [taskId: string] }>()

const summary = computed(() => recurrenceSummary(props.item.recurrence))
</script>
