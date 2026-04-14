import { http } from './http'

export type RecurrenceFrequency = 'DAILY' | 'WEEKLY' | 'MONTHLY' | 'YEARLY'
export type WeekDay = 'MONDAY' | 'TUESDAY' | 'WEDNESDAY' | 'THURSDAY' | 'FRIDAY' | 'SATURDAY' | 'SUNDAY'

export interface RecurrenceUpsertPayload {
  frequency: RecurrenceFrequency
  intervalValue: number
  daysOfWeek?: WeekDay[]
  dayOfMonth?: number
  startDate: string
  endDate?: string
  timezone: string
}

export interface RecurrenceRule {
  id: string
  taskId: string
  frequency: RecurrenceFrequency
  intervalValue: number
  daysOfWeek?: WeekDay[]
  dayOfMonth?: number
  startDate: string
  endDate?: string
  timezone: string
  active: boolean
}

export const recurrenceService = {
  async fetchRange(userId: string, from: string, to: string) {
    const { data } = await http.get('/recurrences/range', { params: { userId, from, to } })
    return data
  },

  async get(taskId: string) {
    const { data } = await http.get<RecurrenceRule>(`/tasks/${taskId}/recurrence`)
    return data
  },

  async create(taskId: string, payload: RecurrenceUpsertPayload) {
    const { data } = await http.post<RecurrenceRule>(`/tasks/${taskId}/recurrence`, payload)
    return data
  },

  async update(taskId: string, payload: RecurrenceUpsertPayload) {
    const { data } = await http.patch<RecurrenceRule>(`/tasks/${taskId}/recurrence`, payload)
    return data
  },

  async remove(taskId: string) {
    await http.delete(`/tasks/${taskId}/recurrence`)
  }
}
