import type { Country } from '@/features/country/models/country'
import type { OrganisationType } from '@/features/organisation/model/organisationtype'

export interface Organisation {
  id: number
  name: string
  shortName: string
  type: OrganisationType
  country: Country
  organisations: Organisation[]
}
