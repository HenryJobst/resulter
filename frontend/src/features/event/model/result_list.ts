import type { ClassResult } from '@/features/event/model/class_result'

export interface ResultList {
    id: number
    creator: string
    createTime: string
    status: string
    classResults: ClassResult[]
}
