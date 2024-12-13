import type { CupType } from '@/features/cup/model/cuptype'
import type { EventKey } from '@/features/event/model/event_key'
import type { EventRacesCupScore } from '@/features/cup/model/event_races_cup_score'
import type { OrganisationScore } from '@/features/cup/model/organisation_score'
import type { PersonWithScore } from '@/features/cup/model/person_with_score'

export interface CupDetailed {
    id: number
    name: string
    type: CupType | null
    events: EventKey[]
    eventRacesCupScores: EventRacesCupScore[]
    overallOrganisationScores: OrganisationScore[]
    classResultShortNameScores: Map<string, PersonWithScore[]>
}
