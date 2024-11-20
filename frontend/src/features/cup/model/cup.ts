import type { CupType } from '@/features/cup/model/cuptype'
import type { EventKey } from '@/features/event/model/event_key'

export interface Cup {
    id: number
    name: string
    type: CupType
    year: number
    events: EventKey[]
}
