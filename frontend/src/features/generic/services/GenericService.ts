import type { IGenericService } from '@/features/generic/services/IGenericService'
import axiosInstance from '@/features/keycloak/services/api'
import { handleApiError } from '@/utils/HandleError'
import type { GenericEntity } from '@/features/generic/models/GenericEntity'
import type { TableSettings } from '@/features/generic/models/table_settings'
import type { RestResult } from '@/features/generic/models/rest_result'

function getSortParam(field: string, order: number | null | undefined) {
  const direction = order === 1 ? 'asc' : 'desc'
  return `${encodeURIComponent(field)},${direction}`
}

function createUrlSearchParams(tableSettings: TableSettings) {
  const urlSearchParams = new URLSearchParams()
  if (tableSettings.sortField) {
    urlSearchParams.append('sort', getSortParam(tableSettings.sortField, tableSettings.sortOrder))
  }
  if (tableSettings.nullSortOrder != 1) {
    urlSearchParams.append('nullSortOrder', tableSettings.nullSortOrder.toString())
  }
  if (tableSettings.defaultSortOrder != 1) {
    urlSearchParams.append('defaultSortOrder', tableSettings.defaultSortOrder.toString())
  }
  if (tableSettings.multiSortMeta) {
    const sortParams = tableSettings.multiSortMeta
      .filter((meta) => meta.order !== undefined && meta.order !== null && meta.order !== 0) // Filtere Einträge ohne Sortierung
      .map((meta) => {
        return getSortParam(meta.field, meta.order)
      })
    sortParams.forEach((s) => {
      urlSearchParams.append('sort', s)
    })
  }
  if (tableSettings.paginator) {
    if (tableSettings.page) {
      urlSearchParams.append('page', tableSettings.page.toString())
    }
    if (tableSettings.rows) {
      urlSearchParams.append('size', tableSettings.rows.toString())
    }
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
    tableSettings?: TableSettings
  ): Promise<RestResult<T> | null> {
    const urlSearchParams = tableSettings
      ? createUrlSearchParams(tableSettings)
      : new URLSearchParams()
    return await axiosInstance
      .get<RestResult<T>>(`${this.endpoint}`, { params: urlSearchParams })
      .then((response) => response.data)
      .catch((error) => {
        handleApiError(error, t)
        return null
      })
  }

  async getById(id: number, t: (key: string) => string): Promise<T> {
    return await axiosInstance
      .get(`${this.endpoint}/${id}`)
      .then((response) => response.data)
      .catch((error) => {
        handleApiError(error, t)
        return null
      })
  }

  async create<T extends GenericEntity>(
    entity: Omit<T, 'id'>,
    t: (key: string) => string
  ): Promise<T> {
    console.log(entity)
    return await axiosInstance
      .post(this.endpoint, entity)
      .then((response) => response.data)
      .catch((error) => {
        handleApiError(error, t)
        return null
      })
  }

  async update<T extends GenericEntity>(entity: T, t: (key: string) => string): Promise<T> {
    return await axiosInstance
      .put(`${this.endpoint}/${entity.id}`, entity)
      .then((response) => response.data)
      .catch((error) => {
        handleApiError(error, t)
        return null
      })
  }

  async deleteById(id: number, t: (key: string) => string): Promise<void> {
    return await axiosInstance
      .delete(`${this.endpoint}/${id}`)
      .then((response) => response.data)
      .catch((error) => {
        handleApiError(error, t)
        return null
      })
  }
}
