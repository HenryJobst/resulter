import type { Event } from '@/features/event/model/event'
import axiosInstance from '@/features/keycloak/services/api'
import { handleApiError } from '@/utils/HandleError'
import type { EventStatus } from '@/features/event/model/event_status'

const url: string = import.meta.env.VITE_API_ENDPOINT + 'event'
const eventStatusUrl: string = import.meta.env.VITE_API_ENDPOINT + 'event_status'

export class EventService {
  static async getAll(): Promise<Event[] | void> {
    return await axiosInstance
      .get<Event[]>(url)
      .then((response) => {
        return response.data.map((element) => {
          if (element.startTime) {
            element.startTime = new Date(element.startTime)
          }
          return element
        })
      })
      .catch((error) => {
        console.error('Fehler bei der Anfrage:', error)
      })
  }

  static async calculate(id: number | string, t: (key: string) => string) {
    return axiosInstance
      .put(url + '/' + id + '/calculate')
      .then((response) => response.data)
      .catch((error) => {
        handleApiError(error, t)
        return null
      })
  }

  static async getEventStatus(t: (key: string) => string): Promise<EventStatus[] | null> {
    return await axiosInstance
      .get<EventStatus[]>(eventStatusUrl)
      .then((response) => response.data)
      .catch((error) => {
        handleApiError(error, t)
        return null
      })
  }
}
