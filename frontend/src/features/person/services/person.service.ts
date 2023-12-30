import axiosInstance from '@/features/keycloak/services/api'
import type { Person } from '@/features/person/model/person'
import { handleApiError } from '@/utils/HandleError'

const url: string = import.meta.env.VITE_API_ENDPOINT + 'person'

export class PersonService {
  static async getAll(t: (key: string) => string): Promise<Person[] | null> {
    return await axiosInstance
      .get<Person[]>(url)
      .then((response) => response.data)
      .catch((error) => {
        handleApiError(error, t)
        return null
      })
  }
}
