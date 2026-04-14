<template>
  <BasePage>
    <template #title>Tableau de bord</template>
    <template #subtitle>Vue synthétique des listes et tâches</template>

    <div class="grid-mobile">
      <v-card class="pa-4">
        <div class="text-caption">Listes</div>
        <div class="text-h5">{{ listsCount }}</div>
      </v-card>
      <v-card class="pa-4">
        <div class="text-caption">Tâches</div>
        <div class="text-h5">{{ tasksCount }}</div>
      </v-card>
    </div>
  </BasePage>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useTaskStore } from '../stores/task'
import BasePage from '../components/base/BasePage.vue'

const taskStore = useTaskStore()
const listsCount = computed(() => taskStore.lists.length)
const tasksCount = computed(() => taskStore.tasks.length)
</script>
