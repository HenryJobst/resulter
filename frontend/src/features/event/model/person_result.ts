import type { CupScore } from '@/features/event/model/cup_score'

export interface PersonResult {
  id: number
  position: number
  personName: string
  birthYear: string
  runTime: string
  resultStatus: string
  organisation: string
  cupScores?: CupScore[]
}
