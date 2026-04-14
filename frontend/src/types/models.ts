export interface TaskList {
  id: string
  name: string
  color?: string
  position?: number
  archived?: boolean
}

export interface TaskItem {
  id: string
  taskListId: string
  createdByUserId: string
  title: string
  description?: string
  status: 'TODO' | 'IN_PROGRESS' | 'DONE' | 'BLOCKED' | 'CANCELED'
  dueAt?: string
  startAt?: string
  completedAt?: string
  allDay?: boolean
}
