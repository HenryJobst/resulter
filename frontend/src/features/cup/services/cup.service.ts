import axiosInstance from '@/features/keycloak/services/api'
import type { Cup } from '@/features/cup/model/cup'
import { handleApiError } from '@/utils/HandleError'

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
}
