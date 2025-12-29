import type { CupOverallStatistics } from './cup_overall_statistics'
import type { OrganisationStatistics } from './organisation_statistics'

export interface CupStatistics {
    overallStatistics: CupOverallStatistics
    organisationStatistics: OrganisationStatistics[]
}
