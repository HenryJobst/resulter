import type { SportEvent } from '@/features/event/model/sportEvent'
import type { RaceCupScore } from '@/features/cup/model/race_cup_score'

export interface EventRacesCupScore {
    event: SportEvent
    raceCupScores: RaceCupScore[]
}
