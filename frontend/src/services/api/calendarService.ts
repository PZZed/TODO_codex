import { http } from './http'

export const calendarService = {
  async enableIcs(userId: string) {
    const { data } = await http.post('/calendar/integrations/ics', null, { params: { userId } })
    return data
  },
  async rotateIcsToken(userId: string) {
    const { data } = await http.post(`/calendar/integrations/ics/${userId}/rotate-token`)
    return data
  }
}
