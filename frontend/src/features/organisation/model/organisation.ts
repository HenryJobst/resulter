export interface Organisation {
  id: number
  name: string
  shortName: string
  type: string
  organisations: Organisation[]
}
