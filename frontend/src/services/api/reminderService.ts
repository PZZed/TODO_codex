import { http } from './http'

interface CreateTaskReminderPayload {
  minutesBeforeDue: number
}

export const reminderService = {
  async createForTask(taskId: string, payload: CreateTaskReminderPayload) {
    const { data } = await http.post(`/tasks/${taskId}/reminders`, {
      type: 'DUE_SOON',
      triggerMode: 'RELATIVE_DUE',
      minutesBeforeDue: payload.minutesBeforeDue,
      channel: 'IN_APP'
    })
    return data
  }
}
