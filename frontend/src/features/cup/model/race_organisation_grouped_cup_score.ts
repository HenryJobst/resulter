import type { Race } from '@/features/race/model/race'
import type { OrganisationScore } from '@/features/cup/model/organisation_score'

export interface RaceOrganisationGroupedCupScore {
    race: Race
    organisationScores: OrganisationScore[]
}
