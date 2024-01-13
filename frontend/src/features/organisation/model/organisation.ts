import type { OrganisationType } from '@/features/organisation/model/organisationtype'

export interface Organisation {
  id: number
  name: string
  shortName: string
  type: OrganisationType
  countryId: number
  organisationIds: number[]
}
