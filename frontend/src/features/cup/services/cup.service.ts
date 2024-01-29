import axiosInstance from '@/features/keycloak/services/api'
import type { Cup } from '@/features/cup/model/cup'
import { handleApiError } from '@/utils/HandleError'
import type { CupResults } from '@/features/cup/model/cup_results'
import type { CupType } from '@/features/cup/model/cuptype'

const url: string = import.meta.env.VITE_API_ENDPOINT + 'cup'
const cupTypeUrl: string = import.meta.env.VITE_API_ENDPOINT + 'cup_types'

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

  static async getCupTypes(t: (key: string) => string): Promise<CupType[] | null> {
    return await axiosInstance
      .get<CupType[]>(cupTypeUrl)
      .then((response) => response.data)
      .catch((error) => {
        handleApiError(error, t)
        return null
      })
  }

  static async getById(id: number, t: (key: string) => string): Promise<Cup> {
    return await axiosInstance
      .get(url + '/' + id)
      .then((response) => response.data)
      .catch((error) => {
        handleApiError(error, t)
        return null
      })
  }

  static async getResultsById(id: string, t: (key: string) => string): Promise<CupResults> {
    return await axiosInstance
      .get(`${url}/${id}/results`)
      .then((response) => response.data)
      .catch((error) => {
        handleApiError(error, t)
        return null
      })
  }

  static async update(cup: Cup, t: (key: string) => string): Promise<Cup | null> {
    return await axiosInstance
      .put(`${url}/${cup.id}`, cup)
      .then((response) => response.data)
      .catch((error) => {
        handleApiError(error, t)
        return null
      })
  }

  static async create(cup: Omit<Cup, 'id'>, t: (key: string) => string): Promise<Cup> {
    return await axiosInstance
      .post(url, cup)
      .then((response) => response.data)
      .catch((error) => {
        handleApiError(error, t)
        return null
      })
  }

  static async deleteById(id: number | string, t: (key: string) => string) {
    return await axiosInstance
      .delete(url + '/' + id)
      .then((response) => response.data)
      .catch((error) => {
        handleApiError(error, t)
        return null
      })
  }
}
