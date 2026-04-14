<template>
  <v-dialog v-model="model" max-width="720">
    <v-card>
      <v-card-title>{{ mode === 'create' ? 'Créer une tâche récurrente' : 'Modifier la tâche récurrente' }}</v-card-title>
      <v-card-text>
        <v-form class="d-flex flex-column ga-3" @submit.prevent="submit" aria-label="Formulaire récurrence">
          <v-select
            v-if="mode === 'create'"
            v-model="draft.taskListId"
            :items="taskListOptions"
            item-title="name"
            item-value="id"
            label="Liste"
            :error-messages="errors.taskListId"
          />

          <v-text-field v-model="draft.title" label="Titre" autofocus :error-messages="errors.title" maxlength="255" required />

          <v-textarea v-model="draft.description" label="Description" rows="2" auto-grow />

          <v-select v-model="draft.frequency" :items="frequencyOptions" label="Fréquence" />

          <v-text-field v-model.number="draft.intervalValue" type="number" min="1" step="1" label="Intervalle" :error-messages="errors.intervalValue" />

          <v-select
            v-if="draft.frequency === 'WEEKLY'"
            v-model="draft.daysOfWeek"
            :items="weekdayOptions"
            item-title="label"
            item-value="value"
            label="Jours de semaine"
            chips
            multiple
            :error-messages="errors.daysOfWeek"
          />

          <v-text-field
            v-if="draft.frequency === 'MONTHLY'"
            v-model.number="draft.dayOfMonth"
            type="number"
            min="1"
            max="31"
            label="Jour du mois"
            :error-messages="errors.dayOfMonth"
          />

          <v-text-field v-model="draft.startDate" type="date" label="Date de début" :error-messages="errors.startDate" />
          <v-text-field v-model="draft.endDate" type="date" label="Date de fin" :error-messages="errors.endDate" />
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
import type { TaskList, TaskItem } from '../../types/models'
import type { RecurrenceFrequency, WeekDay, RecurrenceRule } from '../../services/api/recurrenceService'

interface EditableRecurrence {
  task: TaskItem
  recurrence: RecurrenceRule
}

const props = defineProps<{
  modelValue: boolean
  mode: 'create' | 'edit'
  taskLists: TaskList[]
  loading?: boolean
  editable?: EditableRecurrence | null
}>()

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
  submit: [payload: {
    taskListId?: string
    title: string
    description?: string
    frequency: RecurrenceFrequency
    intervalValue: number
    daysOfWeek?: WeekDay[]
    dayOfMonth?: number
    startDate: string
    endDate?: string
  }]
}>()

const model = computed({
  get: () => props.modelValue,
  set: (value: boolean) => emit('update:modelValue', value)
})

const frequencyOptions: RecurrenceFrequency[] = ['DAILY', 'WEEKLY', 'MONTHLY', 'YEARLY']
const weekdayOptions: Array<{ label: string; value: WeekDay }> = [
  { label: 'Lundi', value: 'MONDAY' },
  { label: 'Mardi', value: 'TUESDAY' },
  { label: 'Mercredi', value: 'WEDNESDAY' },
  { label: 'Jeudi', value: 'THURSDAY' },
  { label: 'Vendredi', value: 'FRIDAY' },
  { label: 'Samedi', value: 'SATURDAY' },
  { label: 'Dimanche', value: 'SUNDAY' }
]

const taskListOptions = computed(() => props.taskLists)

const draft = reactive({
  taskListId: '',
  title: '',
  description: '',
  frequency: 'WEEKLY' as RecurrenceFrequency,
  intervalValue: 1,
  daysOfWeek: [] as WeekDay[],
  dayOfMonth: undefined as number | undefined,
  startDate: todayAsDateInput(),
  endDate: ''
})

const errors = reactive({
  taskListId: '',
  title: '',
  intervalValue: '',
  daysOfWeek: '',
  dayOfMonth: '',
  startDate: '',
  endDate: ''
})

watch(
  () => [props.modelValue, props.mode, props.editable],
  () => {
    if (!props.modelValue) {
      return
    }

    if (props.mode === 'edit' && props.editable) {
      draft.taskListId = props.editable.task.taskListId
      draft.title = props.editable.task.title
      draft.description = props.editable.task.description ?? ''
      draft.frequency = props.editable.recurrence.frequency
      draft.intervalValue = props.editable.recurrence.intervalValue
      draft.daysOfWeek = props.editable.recurrence.daysOfWeek ?? []
      draft.dayOfMonth = props.editable.recurrence.dayOfMonth
      draft.startDate = props.editable.recurrence.startDate
      draft.endDate = props.editable.recurrence.endDate ?? ''
    } else {
      draft.taskListId = props.taskLists[0]?.id ?? ''
      draft.title = ''
      draft.description = ''
      draft.frequency = 'WEEKLY'
      draft.intervalValue = 1
      draft.daysOfWeek = []
      draft.dayOfMonth = undefined
      draft.startDate = todayAsDateInput()
      draft.endDate = ''
    }

    resetErrors()
  },
  { immediate: true }
)

function submit() {
  resetErrors()

  if (props.mode === 'create' && !draft.taskListId) {
    errors.taskListId = 'La liste est obligatoire'
  }

  if (!draft.title.trim()) {
    errors.title = 'Le titre est obligatoire'
  }

  if (draft.intervalValue < 1) {
    errors.intervalValue = 'Intervalle invalide'
  }

  if (!draft.startDate) {
    errors.startDate = 'Date de début obligatoire'
  }

  if (draft.endDate && draft.endDate < draft.startDate) {
    errors.endDate = 'La date de fin doit être après le début'
  }

  if (draft.frequency === 'WEEKLY' && draft.daysOfWeek.length === 0) {
    errors.daysOfWeek = 'Sélectionnez au moins un jour'
  }

  if (draft.frequency === 'MONTHLY' && (!draft.dayOfMonth || draft.dayOfMonth < 1 || draft.dayOfMonth > 31)) {
    errors.dayOfMonth = 'Jour du mois invalide'
  }

  if (Object.values(errors).some(Boolean)) {
    return
  }

  emit('submit', {
    taskListId: draft.taskListId,
    title: draft.title.trim(),
    description: draft.description.trim() || undefined,
    frequency: draft.frequency,
    intervalValue: draft.intervalValue,
    daysOfWeek: draft.frequency === 'WEEKLY' ? draft.daysOfWeek : undefined,
    dayOfMonth: draft.frequency === 'MONTHLY' ? draft.dayOfMonth : undefined,
    startDate: draft.startDate,
    endDate: draft.endDate || undefined
  })
}

function resetErrors() {
  errors.taskListId = ''
  errors.title = ''
  errors.intervalValue = ''
  errors.daysOfWeek = ''
  errors.dayOfMonth = ''
  errors.startDate = ''
  errors.endDate = ''
}

function todayAsDateInput() {
  return new Date().toISOString().slice(0, 10)
}
</script>
