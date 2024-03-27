import type { Sort } from '@/features/generic/models/sort'

export interface Pageable {
    pageNumber: number
    pageSize: number
    sort: Sort
    offset: number
    paged: boolean
    unpaged: boolean
}
