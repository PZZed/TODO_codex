import { defineStore } from 'pinia'
import type { TaskItem, TaskList } from '../types/models'

export const useTaskStore = defineStore('task', {
  state: () => ({
    lists: [] as TaskList[],
    tasks: [] as TaskItem[]
  }),
  getters: {
    tasksToday: (state) => state.tasks.filter(t => Boolean(t.dueAt))
  },
  actions: {
    setLists(lists: TaskList[]) {
      this.lists = lists
    },
    setTasks(tasks: TaskItem[]) {
      this.tasks = tasks
    }
  }
})
