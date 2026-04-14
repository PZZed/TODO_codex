import { http } from './http'
import type { TaskList } from '../../types/models'

interface CreateTaskListPayload {
  ownerUserId: string
  name: string
  color?: string
  position?: number
}

export const taskListService = {
  async fetchByUser(userId: string) {
    const { data } = await http.get<TaskList[]>('/task-lists', { params: { ownerUserId: userId } })
    return data
  },

  async create(payload: CreateTaskListPayload) {
    const { data } = await http.post<TaskList>('/task-lists', payload)
    return data
  },

  async rename(taskListId: string, name: string) {
    const { data } = await http.patch<TaskList>(`/task-lists/${taskListId}/name`, { name })
    return data
  },

  async remove(taskListId: string) {
    await http.delete(`/task-lists/${taskListId}`)
  }
}
