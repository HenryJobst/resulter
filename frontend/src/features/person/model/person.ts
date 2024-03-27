import type { Gender } from '@/features/person/model/gender'

export interface Person {
    id: number
    familyName: string
    givenName: string
    gender: Gender
    birthDate: string | Date
}
