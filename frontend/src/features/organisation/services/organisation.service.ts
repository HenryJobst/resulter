import axiosInstance from '@/features/keycloak/services/api'
import type { Organisation } from '@/features/organisation/model/organisation'
import { handleApiError } from '@/utils/HandleError'

const url: string = import.meta.env.VITE_API_ENDPOINT + 'organisation'

export class OrganisationService {
  static async getAll(t: (key: string) => string): Promise<Organisation[] | null> {
    return await axiosInstance
      .get<Organisation[]>(url)
      .then((response) => response.data)
      .catch((error) => {
        handleApiError(error, t)
        return null
      })
  }
}
