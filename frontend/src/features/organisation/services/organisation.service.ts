import axiosInstance from '@/features/keycloak/services/api'
import type { Organisation } from '@/features/organisation/model/organisation'

const url: string = import.meta.env.VITE_API_ENDPOINT + 'organisation'

export class OrganisationService {
  static async getAll(): Promise<Organisation[] | void> {
    return await axiosInstance
      .get<Organisation[]>(url)
      .then((response) => {
        return response.data.map((element) => {
          return element
        })
      })
      .catch((error) => {
        console.error('Fehler bei der Anfrage:', error)
      })
  }
}
