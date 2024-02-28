import type { OrganisationType } from '@/features/organisation/model/organisationtype'
import type { OrganisationKey } from '@/features/organisation/model/organisation_key'
import type { CountryKey } from '@/features/country/models/country_key'

export interface Organisation {
  id: number
  name: string
  shortName: string
  type: OrganisationType
  country: CountryKey
  childOrganisations: OrganisationKey[]
}
