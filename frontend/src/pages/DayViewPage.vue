<template>
  <section>
    <div class="d-flex align-center justify-space-between mb-4 ga-2">
      <h1 class="text-h6">Ma journée</h1>
      <v-chip size="small" variant="tonal">{{ todayLabel }}</v-chip>
    </div>

    <v-select
      v-model="selectedListId"
      :items="listFilters"
      item-title="title"
      item-value="value"
      density="comfortable"
      variant="outlined"
      label="Filtrer par liste"
      class="mb-3"
      aria-label="Filtrer les tâches par liste"
    />

    <v-progress-linear v-if="initialLoading" indeterminate color="primary" class="mb-4" aria-label="Chargement des tâches du jour" />

    <p v-if="pageError" class="text-error mb-3" role="alert">{{ pageError }}</p>

    <h2 v-if="filteredTodayItems.length" class="text-subtitle-2 mb-2">Aujourd’hui</h2>
    <DayTaskItem
      v-for="item in filteredTodayItems"
      :key="`${item.source}-${item.taskId}`"
      :item="item"
      :loading="completeLoadingTaskId === item.taskId"
      @complete="markDone"
    />

    <h2 v-if="filteredOverdueItems.length" class="text-subtitle-2 mb-2 mt-4">En retard</h2>
    <DayTaskItem
      v-for="item in filteredOverdueItems"
      :key="`overdue-${item.taskId}`"
      :item="item"
      :loading="completeLoadingTaskId === item.taskId"
      @complete="markDone"
    />

    <EmptyState
      v-if="!initialLoading && filteredTodayItems.length === 0 && filteredOverdueItems.length === 0"
      title="Rien aujourd’hui"
      subtitle="Profitez-en pour planifier"
    />

    <v-snackbar v-model="snackbar.show" timeout="3000">{{ snackbar.message }}</v-snackbar>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import DayTaskItem from '../components/day/DayTaskItem.vue'
import EmptyState from '../components/common/EmptyState.vue'
import { taskService, type TaskDayResponse } from '../services/api/taskService'
import { recurrenceService } from '../services/api/recurrenceService'
import { taskListService } from '../services/api/taskListService'
import { toAppError } from '../services/errors/errorMapper'
import { useA11yStore } from '../stores/a11y'
import type { TaskItem, TaskList } from '../types/models'

const DEMO_USER_ID = 'demo-user-id'

type DayOccurrence = { taskId: string; title: string; occurrenceDate: string; source: string }
type DayItem = {
  taskId: string
  title: string
  status: TaskItem['status']
  source: 'task' | 'recurrence' | 'overdue'
  sourceLabel: string
  listId?: string
  listName?: string
  dueAt?: string
  dueLabel?: string
  overdue: boolean
}

const initialLoading = ref(false)
const completeLoadingTaskId = ref('')
const pageError = ref('')

const selectedListId = ref('ALL')
const taskLists = ref<TaskList[]>([])
const todayItems = ref<DayItem[]>([])
const overdueItems = ref<DayItem[]>([])

const snackbar = reactive({ show: false, message: '' })
const a11y = useA11yStore()

const today = new Date()
const todayDateInput = today.toISOString().slice(0, 10)
const todayLabel = new Intl.DateTimeFormat('fr-FR', { dateStyle: 'full' }).format(today)

const listFilters = computed(() => [
  { title: 'Toutes les listes', value: 'ALL' },
  ...taskLists.value.map((list) => ({ title: list.name, value: list.id }))
])

const filteredTodayItems = computed(() => filterByList(sortSmart(todayItems.value)))
const filteredOverdueItems = computed(() => filterByList(sortSmart(overdueItems.value)))

onMounted(async () => {
  await loadDay()
})

async function loadDay() {
  initialLoading.value = true
  pageError.value = ''

  try {
    const [lists, dayData, occurrences] = await Promise.all([
      taskListService.fetchByUser(DEMO_USER_ID),
      taskService.fetchDay(DEMO_USER_ID, todayDateInput) as Promise<TaskDayResponse>,
      recurrenceService.fetchRange(DEMO_USER_ID, todayDateInput, todayDateInput) as Promise<DayOccurrence[]>
    ])

    taskLists.value = lists

    const allTasksByList = await Promise.all(lists.map((list) => taskService.fetchByList(list.id)))
    const allTasks = allTasksByList.flat()

    const taskById = new Map(allTasks.map((task) => [task.id, task]))
    const listNameById = new Map(lists.map((list) => [list.id, list.name]))

    todayItems.value = [
      ...dayData.tasks.map((task) => toDayItem(task, listNameById, 'task')),
      ...occurrences
        .filter((occ) => !dayData.tasks.some((task) => task.id === occ.taskId))
        .map((occ) => {
          const originalTask = taskById.get(occ.taskId)
          return {
            taskId: occ.taskId,
            title: occ.title,
            status: originalTask?.status ?? 'TODO',
            source: 'recurrence' as const,
            sourceLabel: 'Récurrence',
            listId: originalTask?.taskListId,
            listName: originalTask?.taskListId ? listNameById.get(originalTask.taskListId) : undefined,
            dueAt: undefined,
            dueLabel: undefined,
            overdue: false
          }
        })
    ]

    const endOfToday = new Date(`${todayDateInput}T23:59:59.999Z`)
    const dayTaskIds = new Set(todayItems.value.map((item) => item.taskId))

    overdueItems.value = allTasks
      .filter((task) => {
        if (!task.dueAt || task.status === 'DONE') {
          return false
        }
        const dueAtDate = new Date(task.dueAt)
        return dueAtDate < endOfToday && !dayTaskIds.has(task.id)
      })
      .map((task) => toDayItem(task, listNameById, 'overdue'))
  } catch (error) {
    pageError.value = toAppError(error).message
  } finally {
    initialLoading.value = false
  }
}

async function markDone(taskId: string) {
  completeLoadingTaskId.value = taskId
  try {
    const updatedTask = await taskService.complete(taskId)

    todayItems.value = todayItems.value.map((item) =>
      item.taskId === taskId ? { ...item, status: updatedTask.status, overdue: false } : item
    )

    overdueItems.value = overdueItems.value.filter((item) => item.taskId !== taskId)
    showSnackbar('Tâche terminée')
  } catch (error) {
    pageError.value = toAppError(error).message
  } finally {
    completeLoadingTaskId.value = ''
  }
}

function toDayItem(task: TaskItem, listNameById: Map<string, string>, source: 'task' | 'overdue'): DayItem {
  return {
    taskId: task.id,
    title: task.title,
    status: task.status,
    source,
    sourceLabel: source === 'overdue' ? 'En retard' : 'Tâche',
    listId: task.taskListId,
    listName: listNameById.get(task.taskListId),
    dueAt: task.dueAt,
    dueLabel: task.dueAt ? formatDue(task.dueAt) : undefined,
    overdue: source === 'overdue'
  }
}

function sortSmart(items: DayItem[]) {
  return [...items].sort((a, b) => {
    if (a.status === 'DONE' && b.status !== 'DONE') return 1
    if (a.status !== 'DONE' && b.status === 'DONE') return -1

    if (a.overdue !== b.overdue) return a.overdue ? -1 : 1

    const aDate = a.dueAt ? new Date(a.dueAt).getTime() : Number.POSITIVE_INFINITY
    const bDate = b.dueAt ? new Date(b.dueAt).getTime() : Number.POSITIVE_INFINITY
    if (aDate !== bDate) return aDate - bDate

    return a.title.localeCompare(b.title, 'fr')
  })
}

function filterByList(items: DayItem[]) {
  if (selectedListId.value === 'ALL') {
    return items
  }
  return items.filter((item) => item.listId === selectedListId.value)
}

function formatDue(iso: string) {
  const date = new Date(iso)
  if (Number.isNaN(date.getTime())) {
    return iso
  }
  return `Échéance ${new Intl.DateTimeFormat('fr-FR', { dateStyle: 'short', timeStyle: 'short' }).format(date)}`
}

function showSnackbar(message: string) {
  snackbar.show = true
  snackbar.message = message
  a11y.announce(message)
}
</script>
