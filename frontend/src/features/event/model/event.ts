export interface Event {
  id: number
  name: string
  startTime: string | Date
  classes: number
  participants: number
  organisations: number[]
  cups: number[]
}
