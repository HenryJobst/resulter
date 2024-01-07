import type { Organisation } from '@/features/organisation/model/organisation'

export interface Event {
  id: number
  name: string
  startTime: string | Date
  classes: number
  participants: number
  organisations: Organisation[]
}
