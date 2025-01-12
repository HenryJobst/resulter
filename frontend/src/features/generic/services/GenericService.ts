import type { DataTableFilterMetaData } from 'primevue/datatable'
import { sfAnd, sfEqual, sfLike } from 'spring-filter-query-builder'
import type { IGenericService } from '@/features/generic/services/IGenericService'
import axiosInstance from '@/features/keycloak/services/api'
import type { GenericEntity } from '@/features/generic/models/GenericEntity'
import type { TableSettings } from '@/features/generic/models/table_settings'
import type { RestPageResult } from '@/features/generic/models/rest_page_result'

function getSortParam(field: string | ((item: any) => string), order: number | null | undefined) {
    const direction = order === 1 ? 'asc' : 'desc'
    if (typeof field === 'function')
        return `${encodeURIComponent(field(null))},${direction}`

    return `${encodeURIComponent(field)},${direction}`
}

function createUrlSearchParams(tableSettings: TableSettings) {
    const urlSearchParams = new URLSearchParams()
    if (tableSettings.sortField) {
        urlSearchParams.append(
            'sort',
            getSortParam(tableSettings.sortField, tableSettings.sortOrder),
        )
    }

    if (tableSettings.nullSortOrder !== 1)
        urlSearchParams.append('nullSortOrder', tableSettings.nullSortOrder.toString())

    if (tableSettings.defaultSortOrder !== 1)
        urlSearchParams.append('defaultSortOrder', tableSettings.defaultSortOrder.toString())

    if (tableSettings.multiSortMeta) {
        const sortParams = tableSettings.multiSortMeta
            .filter(meta => meta.field != null && meta.order != null && meta.order !== 0) // Filtere Einträge ohne Sortierung
            .map((meta) => {
                return getSortParam(meta.field!, meta.order)
            })
        sortParams.forEach((s) => {
            urlSearchParams.append('sort', s)
        })
    }
    if (tableSettings.paginator) {
        if (tableSettings.page)
            urlSearchParams.append('page', tableSettings.page.toString())

        if (tableSettings.rows)
            urlSearchParams.append('size', tableSettings.rows.toString())
    }

    if (tableSettings.filters) {
        let filterParam: any = null
        Object.keys(tableSettings.filters).forEach((key) => {
            const filter = tableSettings.filters![key] as DataTableFilterMetaData
            if (filter.value) {
                if (!filterParam)
                    filterParam = sfEqual('1', '1')
                switch (filter.matchMode) {
                    case 'equals':
                        filterParam = sfAnd([filterParam, sfEqual(key, filter.value)])
                        break
                    case 'contains':
                        filterParam = sfAnd([filterParam, sfLike(key, filter.value, true)])
                        break
                }
            }
        })
        if (filterParam)
            urlSearchParams.append('filter', filterParam.toString())
    }
    return urlSearchParams
}

export class GenericService<T> implements IGenericService<T> {
    private readonly endpoint: string

    constructor(endpoint: string) {
        this.endpoint = endpoint
    }

    async getAll(
        t: (key: string) => string,
        tableSettings?: TableSettings,
    ): Promise<RestPageResult<T> | null> {
        const urlSearchParams = tableSettings
            ? createUrlSearchParams(tableSettings)
            : new URLSearchParams()
        return await axiosInstance
            .get<RestPageResult<T>>(`${this.endpoint}`, { params: urlSearchParams })
            .then(response => response.data)
    }

    async getAllUnpaged(
        _t: (key: string) => string,
    ): Promise<T[] | null> {
        return await axiosInstance
            .get<T[]>(`${this.endpoint}/all`)
            .then(response => response.data)
    }

    async getById(id: number, _t: (key: string) => string): Promise<T> {
        return await axiosInstance
            .get(`${this.endpoint}/${id}`)
            .then(response => response.data)
    }

    async create<T extends GenericEntity>(
        entity: Omit<T, 'id'>,
        _t: (key: string) => string,
    ): Promise<T> {
        return await axiosInstance
            .post(this.endpoint, entity)
            .then(response => response.data)
    }

    async update<T extends GenericEntity>(entity: T, _t: (key: string) => string): Promise<T> {
        return await axiosInstance
            .put(`${this.endpoint}/${entity.id}`, entity)
            .then(response => response.data)
    }

    async deleteById(id: number, _t: (key: string) => string): Promise<void> {
        return await axiosInstance
            .delete(`${this.endpoint}/${id}`)
            .then(response => response.data)
    }
}
