import axiosInstance from '@/features/keycloak/services/api'
import type { Cup } from '@/features/cup/model/cup'
import { handleApiError } from '@/utils/HandleError'
import type { CupResults } from '@/features/cup/model/cup_results'

const url: string = import.meta.env.VITE_API_ENDPOINT + 'cup'

export class CupService {
  static async getAll(t: (key: string) => string): Promise<Cup[] | null> {
    return await axiosInstance
      .get<Cup[]>(url)
      .then((response) => response.data)
      .catch((error) => {
        handleApiError(error, t)
        return null
      })
  }

  static async getById(id: string, t: (key: string) => string): Promise<Cup> {
    return await axiosInstance
      .get(url + '/' + id)
      .then((response) => response.data)
      .catch((error) => {
        handleApiError(error, t)
        return null
      })
  }

  static async getResultsById(id: string): Promise<CupResults> {
    const response = await axiosInstance.get(url + '/' + id + '/results')
    return response.data
  }

  static async update(cup: Cup, t: (key: string) => string): Promise<Cup | null> {
    return await axiosInstance
      .put(url + '/' + cup.id, cup)
      .then((response) => response.data)
      .catch((error) => {
        handleApiError(error, t)
        return null
      })
  }

  /*
    static async create(event: Omit<Event, 'id'>): Promise<Event> {
      const response = await axiosInstance.post(url, event)
      return response.data
    }
  
   */
}
