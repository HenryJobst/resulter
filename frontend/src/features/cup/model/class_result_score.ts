import type { PersonWithScore } from '@/features/cup/model/person_with_score'

export interface ClassResultScore {
    classResultShortName: string
    personWithScores: PersonWithScore[]
}
