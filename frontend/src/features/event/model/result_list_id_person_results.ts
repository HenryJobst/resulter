import type { CupScoreList } from '@/features/event/model/cup_score_list'
import type { PersonResult } from '@/features/event/model/person_result'

export interface ResultListIdPersonResults {
    resultListId: number
    classResultShortName: string
    personResults: PersonResult[]
    certificateEnabled: boolean | undefined
    cupScoreEnabled: boolean | undefined
    cupScoreLists: CupScoreList[] | undefined
}
