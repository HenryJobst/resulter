import type { ClassResultScore } from '@/features/cup/model/class_result_score'
import type { Race } from '@/features/race/model/race'

export interface RaceClassResultGroupedCupScore {
    race: Race
    classResultScores: ClassResultScore[]
}
