import type { IGenericService } from '@/features/generic/services/IGenericService'
import axiosInstance from '@/features/keycloak/services/api'
import { handleApiError } from '@/utils/HandleError'
import type { GenericEntity } from '@/features/generic/models/GenericEntity'
import type { TableSettings } from '@/features/generic/models/table_settings'

export class GenericService<T> implements IGenericService<T> {
  private readonly endpoint: string

  constructor(endpoint: string) {
    this.endpoint = endpoint
  }

  async getAll(t: (key: string) => string, tableSettings: TableSettings): Promise<T[] | null> {
    return await axiosInstance
      .get<T[]>(
        `${this.endpoint}`,
        tableSettings.paginator
          ? {
              params: {
                page: tableSettings.page,
                size: tableSettings.rows
              }
            }
          : {}
      )
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
