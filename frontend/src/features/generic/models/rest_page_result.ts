import type { Page } from '@/features/generic/models/page'

export interface RestPageResult<T> {
    content: T[]
    page: Page
}
