import { ref } from 'vue'

export type FormErrors = Record<string, string>

export function useFormErrors() {
  const errors = ref<FormErrors>({})

  function setFieldError(field: string, message: string) {
    errors.value[field] = message
  }

  function clearErrors() {
    errors.value = {}
  }

  function fromApi(details: string[] = []) {
    clearErrors()
    for (const detail of details) {
      const [field, ...rest] = detail.split(':')
      if (!field || rest.length === 0) continue
      setFieldError(field.trim(), rest.join(':').trim())
    }
  }

  return { errors, setFieldError, clearErrors, fromApi }
}
