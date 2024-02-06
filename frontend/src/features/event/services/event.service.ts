import type { SportEvent } from '@/features/event/model/sportEvent'
import axiosInstance from '@/features/keycloak/services/api'
import { handleApiError } from '@/utils/HandleError'
import type { EventStatus } from '@/features/event/model/event_status'
import type { EventResult } from '@/features/event/model/event_result'
import { GenericService } from '@/features/generic/services/GenericService'
import type { ResultList } from '@/features/event/model/result_list'

const eventUrl: string = '/event'
const eventStatusUrl: string = '/event_status'

export class EventService extends GenericService<SportEvent> {
  constructor() {
    super(eventUrl)
  }

  async getAll(t: (key: string) => string): Promise<SportEvent[] | null> {
    return await super.getAll(t).then((response) => {
      if (response) {
        return response.map((element) => {
          if (element.startTime) {
            element.startTime = new Date(element.startTime)
          }
          return element
        })
      }
      return null
    })
  }

  static async calculate(id: number | string, t: (key: string) => string) {
    return axiosInstance
      .put(`${eventUrl}/${id}/calculate`)
      .then((response) => response.data)
      .catch((error) => {
        handleApiError(error, t)
        return null
      })
  }

  static async getEventStatus(t: (key: string) => string): Promise<EventStatus[] | null> {
    return await axiosInstance
      .get<EventStatus[]>(eventStatusUrl)
      .then((response) => response.data)
      .catch((error) => {
        handleApiError(error, t)
        return null
      })
  }

  static async getResultsById(id: string, t: (key: string) => string): Promise<EventResult | null> {
    return await axiosInstance
      .get(`${eventUrl}/${id}/results`)
      .then((response) => {
        if (response) {
          return response.data.map((element: EventResult) => {
            return element.resultLists.map((resultList: ResultList) => {
              if (resultList.createTime) {
                resultList.createTime = new Date(resultList.createTime)
              }
              return resultList
            })
          })
        }
        return null
      })
      .catch((error) => {
        handleApiError(error, t)
        return null
      })
  }
}

export const eventService = new EventService()
