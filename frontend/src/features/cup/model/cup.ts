import type { Event } from '@/features/event/model/event'
import type { CupType } from '@/features/cup/model/cuptype'

export interface Cup {
  id: number
  name: string
  type: CupType
  events: Event[]
}
