import type { EventStatus } from '@/features/event/model/event_status'
import type { OrganisationKey } from '@/features/organisation/model/organisation_key'
import type { CertificateKey } from '@/features/certificate/model/certificate_key'

export interface SportEvent {
    id: number
    name: string
    startTime: string | Date
    state: EventStatus
    organisations: OrganisationKey[]
    certificate: CertificateKey | null
}
