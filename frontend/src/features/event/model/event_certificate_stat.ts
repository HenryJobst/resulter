import type { SportEvent } from '@/features/event/model/sportEvent'
import type { Person } from '@/features/person/model/person'

export interface EventCertificateStat {
    id: number
    event: SportEvent
    person: Person
    generated: Date | string
}
