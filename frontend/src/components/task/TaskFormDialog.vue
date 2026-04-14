<template>
  <v-dialog v-model="model" max-width="640">
    <v-card>
      <v-card-title>{{ mode === 'create' ? 'Nouvelle tâche' : 'Modifier la tâche' }}</v-card-title>
      <v-card-text>
        <v-form @submit.prevent="submit" class="d-flex flex-column ga-3" aria-label="Formulaire tâche">
          <v-text-field
            v-model="draft.title"
            label="Titre" autofocus
            maxlength="255"
            :error-messages="errors.title"
            required
            @keyup.enter="submit"
          />

          <v-textarea
            v-model="draft.description"
            label="Description"
            rows="3"
            auto-grow
            maxlength="2000"
          />

          <v-text-field
            v-model="draft.dueAtLocal"
            type="datetime-local"
            label="Échéance"
            :error-messages="errors.dueAt"
            hint="Optionnel"
            persistent-hint
          />

          <v-text-field
            v-model.number="draft.reminderMinutes"
            type="number"
            min="1"
            step="1"
            label="Rappel (minutes avant échéance)"
            :disabled="!draft.dueAtLocal"
            :hint="draft.dueAtLocal ? 'Optionnel' : 'Définissez une échéance pour activer le rappel'"
            persistent-hint
          />
        </v-form>
      </v-card-text>
      <v-card-actions>
        <v-spacer />
        <v-btn variant="text" :disabled="loading" @click="model = false">Annuler</v-btn>
        <v-btn color="primary" :loading="loading" :disabled="loading" @click="submit">
          {{ mode === 'create' ? 'Créer' : 'Enregistrer' }}
        </v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>

<script setup lang="ts">
import { computed, reactive, watch } from 'vue'
import type { TaskItem } from '../../types/models'

const props = defineProps<{
  modelValue: boolean
  mode: 'create' | 'edit'
  loading?: boolean
  task?: TaskItem | null
}>()

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
  submit: [payload: { title: string; description?: string; dueAt?: string; reminderMinutes?: number }]
}>()

const model = computed({
  get: () => props.modelValue,
  set: (value: boolean) => emit('update:modelValue', value)
})

const draft = reactive({
  title: '',
  description: '',
  dueAtLocal: '',
  reminderMinutes: undefined as number | undefined
})

const errors = reactive({
  title: '',
  dueAt: ''
})

watch(
  () => [props.modelValue, props.task, props.mode],
  () => {
    if (!props.modelValue) {
      return
    }

    draft.title = props.task?.title ?? ''
    draft.description = props.task?.description ?? ''
    draft.dueAtLocal = toLocalInput(props.task?.dueAt)
    draft.reminderMinutes = undefined
    errors.title = ''
    errors.dueAt = ''
  },
  { immediate: true }
)

function submit() {
  errors.title = ''
  errors.dueAt = ''

  const title = draft.title.trim()
  if (!title) {
    errors.title = 'Le titre est obligatoire'
    return
  }

  let dueAt: string | undefined
  if (draft.dueAtLocal) {
    const converted = localInputToIso(draft.dueAtLocal)
    if (!converted) {
      errors.dueAt = 'Date invalide'
      return
    }
    dueAt = converted
  }

  if (draft.reminderMinutes !== undefined && draft.reminderMinutes < 1) {
    return
  }

  emit('submit', {
    title,
    description: draft.description.trim() || undefined,
    dueAt,
    reminderMinutes: draft.reminderMinutes
  })
}

function toLocalInput(isoValue?: string) {
  if (!isoValue) {
    return ''
  }
  const date = new Date(isoValue)
  if (Number.isNaN(date.getTime())) {
    return ''
  }
  const offsetMs = date.getTimezoneOffset() * 60_000
  return new Date(date.getTime() - offsetMs).toISOString().slice(0, 16)
}

function localInputToIso(localValue: string) {
  const date = new Date(localValue)
  if (Number.isNaN(date.getTime())) {
    return undefined
  }
  return date.toISOString()
}
</script>
