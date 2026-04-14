import type { AxiosError } from 'axios'
import type { AppError } from './AppError'

export function toAppError(error: unknown): AppError {
  const axiosError = error as AxiosError<{ message?: string; details?: string[] }>
  return {
    status: axiosError.response?.status ?? 500,
    message: axiosError.response?.data?.message ?? axiosError.message ?? 'Unexpected error',
    details: axiosError.response?.data?.details ?? []
  }
}
