import type { PersonResult } from '@/features/event/model/person_result'

export interface ResultListIdPersonResults {
  resultListId: number
  classResultShortName: string
  personResults: PersonResult[]
  certificateEnabled: boolean | undefined
}
