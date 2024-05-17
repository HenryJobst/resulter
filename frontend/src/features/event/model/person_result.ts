import type { CupScore } from '@/features/event/model/cup_score'

export interface PersonResult {
    position: number
    personId: number
    runTime: string
    resultStatus: string
    organisationId: number
    raceNumber: string
    cupScores?: CupScore[]
}
