import type { RecurrenceRule } from '../services/api/recurrenceService'

const weekdays: Record<string, string> = {
  MONDAY: 'lun',
  TUESDAY: 'mar',
  WEDNESDAY: 'mer',
  THURSDAY: 'jeu',
  FRIDAY: 'ven',
  SATURDAY: 'sam',
  SUNDAY: 'dim'
}

export function recurrenceSummary(rule: Pick<RecurrenceRule, 'frequency' | 'intervalValue' | 'daysOfWeek' | 'dayOfMonth' | 'startDate' | 'endDate'>) {
  const every = rule.intervalValue > 1 ? `toutes les ${rule.intervalValue}` : 'toutes les'

  let core = ''
  if (rule.frequency === 'DAILY') {
    core = rule.intervalValue > 1 ? `Tous les ${rule.intervalValue} jours` : 'Tous les jours'
  } else if (rule.frequency === 'WEEKLY') {
    const days = (rule.daysOfWeek ?? []).map((d) => weekdays[d] ?? d).join(', ')
    core = `${every} semaines${days ? ` (${days})` : ''}`
  } else if (rule.frequency === 'MONTHLY') {
    core = rule.dayOfMonth ? `${every} mois, le ${rule.dayOfMonth}` : `${every} mois`
  } else {
    core = rule.intervalValue > 1 ? `Tous les ${rule.intervalValue} ans` : 'Tous les ans'
  }

  const end = rule.endDate ? `, jusqu'au ${formatDate(rule.endDate)}` : ''
  return `${core}, à partir du ${formatDate(rule.startDate)}${end}`
}

function formatDate(value: string) {
  const date = new Date(`${value}T00:00:00`)
  if (Number.isNaN(date.getTime())) {
    return value
  }
  return new Intl.DateTimeFormat('fr-FR', { dateStyle: 'medium' }).format(date)
}
