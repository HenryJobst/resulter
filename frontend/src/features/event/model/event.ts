import type { EventStatus } from '@/features/event/model/event_status'

export interface Event {
  id: number
  name: string
  startTime: string | Date
  state: EventStatus
  organisations: number[]
}
