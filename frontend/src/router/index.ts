import { createRouter, createWebHistory } from 'vue-router'
import DashboardPage from '../pages/DashboardPage.vue'
import TaskListsPage from '../pages/TaskListsPage.vue'
import TaskListDetailPage from '../pages/TaskListDetailPage.vue'
import DayViewPage from '../pages/DayViewPage.vue'
import RecurrencesPage from '../pages/RecurrencesPage.vue'
import SettingsCalendarPage from '../pages/SettingsCalendarPage.vue'

export const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', name: 'dashboard', component: DashboardPage },
    { path: '/lists', name: 'lists', component: TaskListsPage },
    { path: '/lists/:id', name: 'list-detail', component: TaskListDetailPage, props: true },
    { path: '/day', name: 'day-view', component: DayViewPage },
    { path: '/recurrences', name: 'recurrences', component: RecurrencesPage },
    { path: '/settings/calendar', name: 'settings-calendar', component: SettingsCalendarPage }
  ]
})
