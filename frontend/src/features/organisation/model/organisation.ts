import type { Country } from '@/features/country/models/country'

export interface Organisation {
  id: number
  name: string
  shortName: string
  type: OrientationType
  country: Country
  organisations: Organisation[]
}
