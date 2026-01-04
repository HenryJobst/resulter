import type { CertificateKey } from '@/features/certificate/model/certificate_key'
import type { Discipline } from '@/features/event/model/discipline'
import type { EventStatus } from '@/features/event/model/event_status'
import type { OrganisationKey } from '@/features/organisation/model/organisation_key'

export interface SportEvent {
    id: number | undefined
    name: string
    startTime: string | Date
    state: EventStatus
    organisations: OrganisationKey[]
    certificate: CertificateKey | null
    hasSplitTimes: boolean
    discipline: Discipline
    aggregateScore: boolean
}
