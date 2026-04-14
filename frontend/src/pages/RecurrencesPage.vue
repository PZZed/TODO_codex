<template>
  <section>
    <div class="d-flex align-center justify-space-between mb-4 ga-2">
      <h1 class="text-h6">Tâches récurrentes</h1>
      <v-btn color="primary" prepend-icon="mdi-plus" @click="openCreate">Ajouter</v-btn>
    </div>

    <v-progress-linear v-if="initialLoading" indeterminate color="primary" class="mb-4" aria-label="Chargement des récurrences" />

    <p v-if="pageError" class="text-error mb-3" role="alert">{{ pageError }}</p>

    <RecurrenceItem
      v-for="item in recurrences"
      :key="item.task.id"
      :item="item"
      @edit="openEdit"
      @delete="openDeleteDialog"
    />

    <EmptyState
      v-if="!initialLoading && recurrences.length === 0"
      title="Aucune récurrence"
      subtitle="Configurez des tâches récurrentes"
    />

    <RecurrenceFormDialog
      v-model="editor.open"
      :mode="editor.mode"
      :task-lists="taskLists"
      :editable="editor.item"
      :loading="saveLoading"
      @submit="saveRecurrence"
    />

    <v-dialog v-model="deleteDialog.open" max-width="520">
      <v-card>
        <v-card-title>Supprimer la récurrence</v-card-title>
        <v-card-text>La tâche restera existante mais ne sera plus récurrente.</v-card-text>
        <v-card-actions>
          <v-spacer />
          <v-btn variant="text" :disabled="deleteLoading" @click="closeDeleteDialog">Annuler</v-btn>
          <v-btn color="error" :loading="deleteLoading" :disabled="deleteLoading" @click="confirmDelete">Supprimer</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <v-snackbar v-model="snackbar.show" timeout="3500">{{ snackbar.message }}</v-snackbar>
  </section>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import EmptyState from '../components/common/EmptyState.vue'
import RecurrenceFormDialog from '../components/recurrence/RecurrenceFormDialog.vue'
import RecurrenceItem, { type RecurrenceListItem } from '../components/recurrence/RecurrenceItem.vue'
import { taskListService } from '../services/api/taskListService'
import { taskService } from '../services/api/taskService'
import { recurrenceService } from '../services/api/recurrenceService'
import { toAppError } from '../services/errors/errorMapper'
import { useA11yStore } from '../stores/a11y'
import type { TaskList } from '../types/models'

const DEMO_USER_ID = 'demo-user-id'

const taskLists = ref<TaskList[]>([])
const recurrences = ref<RecurrenceListItem[]>([])
const initialLoading = ref(false)
const saveLoading = ref(false)
const deleteLoading = ref(false)
const pageError = ref('')
const timezone = Intl.DateTimeFormat().resolvedOptions().timeZone || 'UTC'

const snackbar = reactive({ show: false, message: '' })
const a11y = useA11yStore()

const editor = reactive<{
  open: boolean
  mode: 'create' | 'edit'
  item: RecurrenceListItem | null
}>({
  open: false,
  mode: 'create',
  item: null
})

const deleteDialog = reactive({
  open: false,
  taskId: ''
})

onMounted(async () => {
  await loadRecurrences()
})

async function loadRecurrences() {
  initialLoading.value = true
  pageError.value = ''

  try {
    const lists = await taskListService.fetchByUser(DEMO_USER_ID)
    taskLists.value = lists

    const items: RecurrenceListItem[] = []

    for (const list of lists) {
      const tasks = await taskService.fetchByList(list.id)
      for (const task of tasks) {
        try {
          const recurrence = await recurrenceService.get(task.id)
          if (recurrence.active) {
            items.push({ task, recurrence })
          }
        } catch {
          // tâche sans récurrence: on ignore
        }
      }
    }

    recurrences.value = items
  } catch (error) {
    pageError.value = toAppError(error).message
  } finally {
    initialLoading.value = false
  }
}

function openCreate() {
  editor.mode = 'create'
  editor.item = null
  editor.open = true
}

function openEdit(item: RecurrenceListItem) {
  editor.mode = 'edit'
  editor.item = item
  editor.open = true
}

async function saveRecurrence(payload: {
  taskListId?: string
  title: string
  description?: string
  frequency: 'DAILY' | 'WEEKLY' | 'MONTHLY' | 'YEARLY'
  intervalValue: number
  daysOfWeek?: Array<'MONDAY' | 'TUESDAY' | 'WEDNESDAY' | 'THURSDAY' | 'FRIDAY' | 'SATURDAY' | 'SUNDAY'>
  dayOfMonth?: number
  startDate: string
  endDate?: string
}) {
  saveLoading.value = true

  try {
    if (editor.mode === 'create') {
      const createdTask = await taskService.create({
        taskListId: payload.taskListId as string,
        createdByUserId: DEMO_USER_ID,
        title: payload.title,
        description: payload.description
      })

      const createdRecurrence = await recurrenceService.create(createdTask.id, {
        frequency: payload.frequency,
        intervalValue: payload.intervalValue,
        daysOfWeek: payload.daysOfWeek,
        dayOfMonth: payload.dayOfMonth,
        startDate: payload.startDate,
        endDate: payload.endDate,
        timezone
      })

      recurrences.value = [{ task: createdTask, recurrence: createdRecurrence }, ...recurrences.value]
      showSnackbar('Récurrence créée')
    } else if (editor.item) {
      const updatedTask = await taskService.update(editor.item.task.id, {
        title: payload.title,
        description: payload.description
      })

      const updatedRecurrence = await recurrenceService.update(editor.item.task.id, {
        frequency: payload.frequency,
        intervalValue: payload.intervalValue,
        daysOfWeek: payload.daysOfWeek,
        dayOfMonth: payload.dayOfMonth,
        startDate: payload.startDate,
        endDate: payload.endDate,
        timezone
      })

      recurrences.value = recurrences.value.map((entry) =>
        entry.task.id === editor.item?.task.id ? { task: { ...entry.task, ...updatedTask }, recurrence: updatedRecurrence } : entry
      )
      showSnackbar('Récurrence modifiée')
    }

    editor.open = false
  } catch (error) {
    pageError.value = toAppError(error).message
  } finally {
    saveLoading.value = false
  }
}

function openDeleteDialog(taskId: string) {
  deleteDialog.open = true
  deleteDialog.taskId = taskId
}

function closeDeleteDialog() {
  deleteDialog.open = false
  deleteDialog.taskId = ''
}

async function confirmDelete() {
  deleteLoading.value = true
  try {
    await recurrenceService.remove(deleteDialog.taskId)
    recurrences.value = recurrences.value.filter((entry) => entry.task.id !== deleteDialog.taskId)
    showSnackbar('Récurrence supprimée')
    closeDeleteDialog()
  } catch (error) {
    pageError.value = toAppError(error).message
  } finally {
    deleteLoading.value = false
  }
}

function showSnackbar(message: string) {
  snackbar.message = message
  snackbar.show = true
  a11y.announce(message)
}
</script>
