import type { Person } from '@/features/person/model/person'
import type { SportEvent } from '@/features/event/model/sportEvent'

export interface EventCertificateStat {
    id: number
    event: SportEvent
    person: Person
    generated: Date | string
}
