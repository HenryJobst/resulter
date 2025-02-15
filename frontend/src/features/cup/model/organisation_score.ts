import type { PersonWithScore } from '@/features/cup/model/person_with_score'
import type { Organisation } from '@/features/organisation/model/organisation'

export interface OrganisationScore {
    organisation: Organisation
    score: number
    personWithScores: PersonWithScore[]
}
