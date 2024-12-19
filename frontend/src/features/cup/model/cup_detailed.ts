import type { CupType } from '@/features/cup/model/cuptype'
import type { EventKey } from '@/features/event/model/event_key'
import type { EventRacesCupScore } from '@/features/cup/model/event_races_cup_score'
import type { OrganisationScore } from '@/features/cup/model/organisation_score'
import type { AggregatedPersonScores } from '@/features/cup/model/aggregated_person_scores'

export interface CupDetailed {
    id: number
    name: string
    type: CupType | null
    events: EventKey[]
    eventRacesCupScores: EventRacesCupScore[]
    overallOrganisationScores: OrganisationScore[]
    aggregatedPersonScores: AggregatedPersonScores[]
}
