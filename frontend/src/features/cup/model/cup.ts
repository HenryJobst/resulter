import type { CupType } from '@/features/cup/model/cuptype'

export interface Cup {
    id: number
    name: string
    type: CupType
    eventIds: number[]
}
