import axiosInstance from '@/features/keycloak/services/api'
import type { Organisation } from '@/features/organisation/model/organisation'
import { handleApiError } from '@/utils/HandleError'
import type { OrganisationType } from '@/features/organisation/model/organisationtype'

const url: string = import.meta.env.VITE_API_ENDPOINT + 'organisation'
const organisationTypeUrl: string = import.meta.env.VITE_API_ENDPOINT + 'organisation_types'

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

  static async getOrganisationTypes(
    t: (key: string) => string
  ): Promise<OrganisationType[] | null> {
    return await axiosInstance
      .get<OrganisationType[]>(organisationTypeUrl)
      .then((response) => response.data)
      .catch((error) => {
        handleApiError(error, t)
        return null
      })
  }
}
