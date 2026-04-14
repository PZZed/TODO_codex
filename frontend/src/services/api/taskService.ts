import { http } from './http'
import type { TaskItem } from '../../types/models'

interface CreateTaskPayload {
  taskListId: string
  createdByUserId: string
  title: string
  description?: string
  dueAt?: string
}

interface UpdateTaskPayload {
  title?: string
  description?: string
  dueAt?: string | null
  status?: TaskItem['status']
}

export interface TaskDayResponse {
  date: string
  tasks: TaskItem[]
}

export const taskService = {
  async fetchByList(taskListId: string) {
    const { data } = await http.get<TaskItem[]>('/tasks', { params: { taskListId } })
    return data
  },

  async create(payload: CreateTaskPayload) {
    const { data } = await http.post<TaskItem>('/tasks', {
      ...payload,
      status: 'TODO',
      priority: 'MEDIUM',
      allDay: false
    })
    return data
  },

  async update(taskId: string, payload: UpdateTaskPayload) {
    const { data } = await http.patch<TaskItem>(`/tasks/${taskId}`, payload)
    return data
  },

  async complete(taskId: string) {
    const { data } = await http.post<TaskItem>(`/tasks/${taskId}/complete`)
    return data
  },

  async remove(taskId: string) {
    await http.delete(`/tasks/${taskId}`)
  },

  async fetchDay(userId: string, date: string) {
    const { data } = await http.get<TaskDayResponse>('/tasks/day', { params: { userId, date, timezone: 'UTC' } })
    return data
  }
}
