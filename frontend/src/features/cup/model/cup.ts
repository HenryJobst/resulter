import type { Event } from '@/features/event/model/event'

export interface Cup {
  id: number
  name: string
  type: string
  events: Event[]
}
