<template>
  <v-form @submit.prevent="onSubmit" class="d-flex ga-2" aria-label="Créer une liste">
    <v-text-field
      v-model="name"
      density="comfortable"
      variant="outlined"
      label="Nom de la liste" autofocus
      hide-details="auto"
      :error-messages="nameError"
      maxlength="120"
      class="flex-grow-1"
      @keyup.enter="onSubmit"
    />
    <v-btn type="submit" color="primary" :loading="loading" :disabled="loading">Créer</v-btn>
  </v-form>
</template>

<script setup lang="ts">
import { ref } from 'vue'

const props = defineProps<{ loading?: boolean }>()
const emit = defineEmits<{ create: [name: string] }>()

const name = ref('')
const nameError = ref('')

function onSubmit() {
  nameError.value = ''
  if (!name.value.trim()) {
    nameError.value = 'Le nom est obligatoire'
    return
  }
  emit('create', name.value.trim())
  name.value = ''
}
</script>
