<template>
  <section>
    <div class="d-flex align-center justify-space-between mb-4 ga-2">
      <h1 class="text-h6">Détail de la liste</h1>
      <v-btn color="primary" prepend-icon="mdi-plus" @click="openCreate">Nouvelle tâche</v-btn>
    </div>

    <v-progress-linear
      v-if="initialLoading"
      indeterminate
      color="primary"
      class="mb-4"
      aria-label="Chargement des tâches"
    />

    <p v-if="pageError" class="text-error mb-3" role="alert">{{ pageError }}</p>

    <TaskListDetailItem
      v-for="task in tasks"
      :key="task.id"
      :task="task"
      @complete="markDone"
      @edit="openEdit"
      @delete="openDeleteDialog"
    />

    <EmptyState
      v-if="!initialLoading && tasks.length === 0"
      title="Aucune tâche"
      subtitle="Ajoutez une tâche à cette liste"
    />

    <TaskFormDialog
      v-model="editor.open"
      :mode="editor.mode"
      :task="editor.task"
      :loading="saveLoading"
      @submit="saveTask"
    />

    <v-dialog v-model="deleteDialog.open" max-width="520">
      <v-card>
        <v-card-title>Supprimer la tâche</v-card-title>
        <v-card-text>Cette action est irréversible.</v-card-text>
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
import { useRoute } from 'vue-router'
import TaskFormDialog from '../components/task/TaskFormDialog.vue'
import TaskListDetailItem from '../components/task/TaskListDetailItem.vue'
import EmptyState from '../components/common/EmptyState.vue'
import { taskService } from '../services/api/taskService'
import { reminderService } from '../services/api/reminderService'
import { toAppError } from '../services/errors/errorMapper'
import { useA11yStore } from '../stores/a11y'
import type { TaskItem } from '../types/models'

const DEMO_USER_ID = 'demo-user-id'

const route = useRoute()
const taskListId = route.params.id as string

const tasks = ref<TaskItem[]>([])
const initialLoading = ref(false)
const saveLoading = ref(false)
const deleteLoading = ref(false)
const pageError = ref('')

const snackbar = reactive({ show: false, message: '' })
const a11y = useA11yStore()

const editor = reactive<{
  open: boolean
  mode: 'create' | 'edit'
  task: TaskItem | null
}>({
  open: false,
  mode: 'create',
  task: null
})

const deleteDialog = reactive({
  open: false,
  taskId: ''
})

onMounted(async () => {
  await loadTasks()
})

async function loadTasks() {
  initialLoading.value = true
  pageError.value = ''
  try {
    tasks.value = await taskService.fetchByList(taskListId)
  } catch (error) {
    pageError.value = toAppError(error).message
  } finally {
    initialLoading.value = false
  }
}

function openCreate() {
  editor.mode = 'create'
  editor.task = null
  editor.open = true
}

function openEdit(task: TaskItem) {
  editor.mode = 'edit'
  editor.task = task
  editor.open = true
}

async function saveTask(payload: { title: string; description?: string; dueAt?: string; reminderMinutes?: number }) {
  saveLoading.value = true

  try {
    if (editor.mode === 'create') {
      const createdTask = await taskService.create({
        taskListId,
        createdByUserId: DEMO_USER_ID,
        title: payload.title,
        description: payload.description,
        dueAt: payload.dueAt
      })

      if (payload.dueAt && payload.reminderMinutes) {
        await reminderService.createForTask(createdTask.id, { minutesBeforeDue: payload.reminderMinutes })
      }

      tasks.value = [createdTask, ...tasks.value]
      showSnackbar('Tâche créée')
    } else if (editor.task) {
      const updatedTask = await taskService.update(editor.task.id, {
        title: payload.title,
        description: payload.description,
        dueAt: payload.dueAt ?? null
      })

      if (payload.dueAt && payload.reminderMinutes) {
        await reminderService.createForTask(updatedTask.id, { minutesBeforeDue: payload.reminderMinutes })
      }

      tasks.value = tasks.value.map((task) => (task.id === updatedTask.id ? { ...task, ...updatedTask } : task))
      showSnackbar('Tâche modifiée')
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
    await taskService.remove(deleteDialog.taskId)
    tasks.value = tasks.value.filter((task) => task.id !== deleteDialog.taskId)
    showSnackbar('Tâche supprimée')
    closeDeleteDialog()
  } catch (error) {
    pageError.value = toAppError(error).message
  } finally {
    deleteLoading.value = false
  }
}

async function markDone(taskId: string) {
  try {
    const updatedTask = await taskService.complete(taskId)
    tasks.value = tasks.value.map((task) => (task.id === taskId ? { ...task, ...updatedTask } : task))
    showSnackbar('Tâche terminée')
  } catch (error) {
    pageError.value = toAppError(error).message
  }
}

function showSnackbar(message: string) {
  snackbar.message = message
  snackbar.show = true
  a11y.announce(message)
}
</script>
