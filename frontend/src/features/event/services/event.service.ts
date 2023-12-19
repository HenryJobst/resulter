import type { Event } from '@/features/event/model/event'
import axios from 'axios'

const url: string = import.meta.env.VITE_API_ENDPOINT + 'event'

export class EventService {
  static async getAll(): Promise<Event[] | void> {
    return await axios
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
    const response = await axios.get(url + '/' + id)
    return response.data
  }

  static async create(event: Omit<Event, 'id'>): Promise<Event> {
    const response = await axios.post(url, event)
    return response.data
  }

  static async update(event: Event): Promise<Event> {
    const response = await axios.put(url + '/' + event.id, event)
    return response.data
  }

  static async deleteById(id: number | string) {
    const response = await axios.delete(url + '/' + id)
    return response.data
  }
}
