import type { SportEvent } from '@/features/event/model/sportEvent'
import type { RaceOrganisationGroupedCupScore } from '@/features/cup/model/race_organisation_grouped_cup_score'
import type { RaceClassResultGroupedCupScore } from '@/features/cup/model/race_class_result_grouped_cup_score'

export interface EventRacesCupScore {
    event: SportEvent
    raceOrganisationGroupedCupScores: RaceOrganisationGroupedCupScore[]
    raceClassResultGroupedCupScores: RaceClassResultGroupedCupScore[]
}
