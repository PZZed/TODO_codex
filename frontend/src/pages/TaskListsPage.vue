<template>
  <section>
    <h1 class="text-h6 mb-4">Listes de tâches</h1>

    <TaskListCreateForm :loading="createLoading" @create="handleCreate" />

    <v-progress-linear
      v-if="initialLoading"
      indeterminate
      color="primary"
      class="my-4"
      aria-label="Chargement des listes"
    />

    <p v-if="pageError" class="text-error mt-3" role="alert">{{ pageError }}</p>

    <div class="mt-4">
      <TaskListItem
        v-for="list in store.lists"
        :key="list.id"
        :list="list"
        @open="openList"
        @rename="openRenameDialog"
        @delete="openDeleteDialog"
      />
    </div>

    <EmptyState
      v-if="!initialLoading && store.lists.length === 0"
      title="Aucune liste"
      subtitle="Créez votre première liste"
      class="mt-4"
    />

    <v-dialog v-model="renameDialog.open" max-width="520">
      <v-card>
        <v-card-title>Renommer la liste</v-card-title>
        <v-card-text>
          <v-text-field
            v-model="renameDialog.name"
            label="Nouveau nom"
            maxlength="120"
            :error-messages="renameDialog.error"
            @keyup.enter="submitRename"
          />
        </v-card-text>
        <v-card-actions>
          <v-spacer />
          <v-btn variant="text" :disabled="renameLoading" @click="closeRenameDialog">Annuler</v-btn>
          <v-btn color="primary" :loading="renameLoading" :disabled="renameLoading" @click="submitRename">
            Enregistrer
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <v-dialog v-model="deleteDialog.open" max-width="520">
      <v-card>
        <v-card-title>Supprimer la liste</v-card-title>
        <v-card-text>Cette action est définitive.</v-card-text>
        <v-card-actions>
          <v-spacer />
          <v-btn variant="text" :disabled="deleteLoading" @click="closeDeleteDialog">Annuler</v-btn>
          <v-btn color="error" :loading="deleteLoading" :disabled="deleteLoading" @click="submitDelete">
            Supprimer
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <v-snackbar v-model="snackbar.show" timeout="4000">{{ snackbar.message }}</v-snackbar>
  </section>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import TaskListCreateForm from '../components/task-list/TaskListCreateForm.vue'
import TaskListItem from '../components/task-list/TaskListItem.vue'
import EmptyState from '../components/common/EmptyState.vue'
import { useTaskStore } from '../stores/task'
import { taskListService } from '../services/api/taskListService'
import { toAppError } from '../services/errors/errorMapper'
import { useA11yStore } from '../stores/a11y'
import type { TaskList } from '../types/models'

const DEMO_USER_ID = 'demo-user-id'

const router = useRouter()
const store = useTaskStore()

const initialLoading = ref(false)
const createLoading = ref(false)
const renameLoading = ref(false)
const deleteLoading = ref(false)
const pageError = ref('')

const snackbar = reactive({ show: false, message: '' })
const a11y = useA11yStore()

const renameDialog = reactive<{
  open: boolean
  taskListId: string
  name: string
  error: string
}>({
  open: false,
  taskListId: '',
  name: '',
  error: ''
})

const deleteDialog = reactive({
  open: false,
  taskListId: ''
})

onMounted(async () => {
  await loadLists()
})

async function loadLists() {
  initialLoading.value = true
  pageError.value = ''
  try {
    const lists = await taskListService.fetchByUser(DEMO_USER_ID)
    store.setLists(lists)
  } catch (error) {
    const appError = toAppError(error)
    pageError.value = appError.message
  } finally {
    initialLoading.value = false
  }
}

async function handleCreate(name: string) {
  createLoading.value = true
  pageError.value = ''
  try {
    const createdList = await taskListService.create({
      ownerUserId: DEMO_USER_ID,
      name
    })
    store.setLists([createdList, ...store.lists])
    showSnackbar('Liste créée')
  } catch (error) {
    const appError = toAppError(error)
    pageError.value = appError.message
  } finally {
    createLoading.value = false
  }
}

function openList(id: string) {
  router.push(`/lists/${id}`)
}

function openRenameDialog(list: TaskList) {
  renameDialog.open = true
  renameDialog.taskListId = list.id
  renameDialog.name = list.name
  renameDialog.error = ''
}

function closeRenameDialog() {
  renameDialog.open = false
  renameDialog.taskListId = ''
  renameDialog.name = ''
  renameDialog.error = ''
}

async function submitRename() {
  renameDialog.error = ''
  const newName = renameDialog.name.trim()

  if (!newName) {
    renameDialog.error = 'Le nom est obligatoire'
    return
  }

  renameLoading.value = true

  try {
    const updated = await taskListService.rename(renameDialog.taskListId, newName)
    store.setLists(store.lists.map((list) => (list.id === updated.id ? { ...list, ...updated } : list)))
    showSnackbar('Liste renommée')
    closeRenameDialog()
  } catch (error) {
    const appError = toAppError(error)
    renameDialog.error = appError.message
  } finally {
    renameLoading.value = false
  }
}

function openDeleteDialog(taskListId: string) {
  deleteDialog.open = true
  deleteDialog.taskListId = taskListId
}

function closeDeleteDialog() {
  deleteDialog.open = false
  deleteDialog.taskListId = ''
}

async function submitDelete() {
  deleteLoading.value = true
  try {
    await taskListService.remove(deleteDialog.taskListId)
    store.setLists(store.lists.filter((list) => list.id !== deleteDialog.taskListId))
    showSnackbar('Liste supprimée')
    closeDeleteDialog()
  } catch (error) {
    const appError = toAppError(error)
    pageError.value = appError.message
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
