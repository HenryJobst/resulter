import type { Event } from '@/features/event/model/event'
import axiosInstance from '@/features/keycloak/services/api'
import type { EventResults } from '@/features/event/model/event_results'
import { handleApiError } from '@/utils/HandleError'

const url: string = import.meta.env.VITE_API_ENDPOINT + 'event'

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

  static async getById(id: string): Promise<Event> {
    const response = await axiosInstance.get(url + '/' + id)
    return response.data
  }

  static async getResultsById(id: string): Promise<EventResults> {
    const response = await axiosInstance.get(url + '/' + id + '/results')
    return response.data
  }

  static async create(event: Omit<Event, 'id'>, t: (key: string) => string): Promise<Event> {
    const response = await axiosInstance.post(url, event)
    return await axiosInstance
      .post(url, event)
      .then((response) => response.data)
      .catch((error) => {
        handleApiError(error, t)
        return null
      })
  }

  static async update(event: Event): Promise<Event> {
    const response = await axiosInstance.put(url + '/' + event.id, event)
    return response.data
  }

  static async deleteById(id: number | string) {
    const response = await axiosInstance.delete(url + '/' + id)
    return response.data
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
}
