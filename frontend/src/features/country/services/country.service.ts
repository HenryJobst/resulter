import axiosInstance from '@/features/keycloak/services/api'
import { handleApiError } from '@/utils/HandleError'
import type { Country } from '@/features/country/models/country'

const url: string = import.meta.env.VITE_API_ENDPOINT + 'country'

export class CountryService {
  static async getAll(t: (key: string) => string): Promise<Country[] | null> {
    return await axiosInstance
      .get<Country[]>(url)
      .then((response) => response.data)
      .catch((error) => {
        handleApiError(error, t)
        return null
      })
  }
}
