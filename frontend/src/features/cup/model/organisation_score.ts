import type { Organisation } from '@/features/organisation/model/organisation'
import type { PersonWithScore } from '@/features/cup/model/person_with_score'

export interface OrganisationScore {
    organisation: Organisation
    score: number
    personWithScores: PersonWithScore[]
}
