import type { ClassResult } from '@/features/event/model/class_result'

export interface ResultList {
    id: number
    eventId: number
    raceId: number
    creator: string
    createTime: string | Date
    status: string
    classResults: ClassResult[]
    isCertificateAvailable: boolean
    isCupScoreAvailable: boolean
}
