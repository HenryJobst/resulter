import type { PersonResult } from '@/features/event/model/person_result'

export interface ClassResult {
  shortName: string
  name: string
  personResults: PersonResult[]
}
