import type { PersonResult } from '@/features/event/model/person_result'

export interface ClassResult {
  id: number
  name: string
  personResults: PersonResult[]
}
