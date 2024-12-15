import type { Race } from '@/features/race/model/race'
import type { ClassResultScore } from '@/features/cup/model/class_result_score'

export interface RaceClassResultGroupedCupScore {
    race: Race
    classResultScores: ClassResultScore[]
}
