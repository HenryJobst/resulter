import type { OrganisationScore } from '@/features/cup/model/organisation_score'
import type { Race } from '@/features/race/model/race'

export interface RaceOrganisationGroupedCupScore {
    race: Race
    organisationScores: OrganisationScore[]
}
