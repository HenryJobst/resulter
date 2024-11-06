import type { CupScore } from '@/features/event/model/cup_score'

export interface CupScoreList {
    id: number
    cupId: number
    resultListId: number
    creator: string
    createTime: string | Date
    status: string
    cupScores: CupScore[]
}
