import type { CountryKey } from '@/features/country/models/country_key'
import type { OrganisationKey } from '@/features/organisation/model/organisation_key'
import type { OrganisationType } from '@/features/organisation/model/organisationtype'

export interface Organisation extends OrganisationKey {
    shortName: string
    type: OrganisationType
    country: CountryKey | null
    childOrganisations: OrganisationKey[]
}
