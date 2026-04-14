export interface TaskFormState {
  title: string
  dueAt?: string
}

export function validateTaskForm(input: TaskFormState): Record<string, string> {
  const errors: Record<string, string> = {}
  if (!input.title || input.title.trim().length === 0) {
    errors.title = 'Le titre est obligatoire'
  }
  if (input.title && input.title.length > 255) {
    errors.title = 'Le titre doit contenir au maximum 255 caractères'
  }
  return errors
}
