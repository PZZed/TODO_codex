<template>
  <v-card class="mb-2" :aria-label="`Tâche ${item.title}`">
    <v-card-text class="py-3 d-flex align-start justify-space-between ga-2">
      <div class="d-flex ga-2 flex-grow-1 align-start">
        <v-checkbox-btn
          :model-value="item.status === 'DONE'"
          :disabled="item.status === 'DONE' || loading"
          color="success"
          :aria-label="`Marquer ${item.title} comme terminée`"
          @update:model-value="$emit('complete', item.taskId)"
        />

        <div>
          <div class="font-weight-medium" :class="{ 'text-decoration-line-through': item.status === 'DONE' }">{{ item.title }}</div>
          <div class="text-caption d-flex ga-2 flex-wrap mt-1">
            <v-chip size="x-small" variant="tonal">{{ item.sourceLabel }}</v-chip>
            <v-chip v-if="item.listName" size="x-small" variant="tonal">{{ item.listName }}</v-chip>
            <v-chip v-if="item.overdue" size="x-small" color="error" variant="tonal">En retard</v-chip>
            <span v-if="item.dueLabel">{{ item.dueLabel }}</span>
          </div>
        </div>
      </div>
    </v-card-text>
  </v-card>
</template>

<script setup lang="ts">
defineProps<{
  item: {
    taskId: string
    title: string
    status: 'TODO' | 'IN_PROGRESS' | 'DONE' | 'BLOCKED' | 'CANCELED'
    sourceLabel: string
    listName?: string
    overdue: boolean
    dueLabel?: string
  }
  loading?: boolean
}>()

defineEmits<{ complete: [taskId: string] }>()
</script>
