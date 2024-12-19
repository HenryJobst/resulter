import type { PersonWithScore } from '@/features/cup/model/person_with_score'

export interface AggregatedPersonScores {
    classResultShortName: string
    personWithScoreList: PersonWithScore[]
}
