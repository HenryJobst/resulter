import type { SportEvent } from '@/features/event/model/sportEvent'
import axiosInstance from '@/features/keycloak/services/api'
import { handleApiError } from '@/utils/HandleError'
import type { EventStatus } from '@/features/event/model/event_status'
import type { EventResults } from '@/features/event/model/event_results'
import { GenericService } from '@/features/generic/services/GenericService'
import type { ResultList } from '@/features/event/model/result_list'
import type { TableSettings } from '@/features/generic/models/table_settings'
import type { RestResult } from '@/features/generic/models/rest_result'
import { prettyPrint } from '@base2/pretty-print-object'

const eventUrl: string = '/event'
const resultListUrl: string = '/result_list'
const eventStatusUrl: string = '/event_status'

export class EventService extends GenericService<SportEvent> {
  constructor() {
    super(eventUrl)
  }

  async getAll(
    t: (key: string) => string,
    tableSettings?: TableSettings
  ): Promise<RestResult<SportEvent> | null> {
    return await super.getAll(t, tableSettings).then((response) => {
      if (response) {
        const result = response
        result.content = result.content.map((element) => {
          if (element.startTime) {
            element.startTime = new Date(element.startTime)
          }
          return element
        })
        return result
      }
      return null
    })
  }

  static async calculate(result_list_id: number, t: (key: string) => string) {
    return axiosInstance
      .put(`${resultListUrl}/${result_list_id}/calculate`)
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

  static async getResultsById(
    id: string,
    t: (key: string) => string
  ): Promise<EventResults | null> {
    return await axiosInstance
      .get(`${eventUrl}/${id}/results`)
      .then((response) => {
        if (response) {
          response.data.resultLists = response.data.resultLists.map((resultList: ResultList) => {
            if (resultList.createTime && typeof resultList.createTime === 'string') {
              // Entfernen des Zeitzone-Identifikators, da dieser nicht von Date.parse() unterstützt wird
              const dateStringWithoutTimezone = resultList.createTime.split('[')[0]
              resultList.createTime = new Date(dateStringWithoutTimezone)
            }
            return resultList
          })
          return response.data
        }
        return null
      })
      .catch((error) => {
        handleApiError(error, t)
        return null
      })
  }

  static async upload(formData: FormData, t: (key: string) => string) {
    return axiosInstance
      .post('/upload', formData, {
        headers: {
          'Content-Type': 'multipart/form-data'
        }
      })
      .then((response) => response.data)
      .catch((error) => {
        handleApiError(error, t)
        return null
      })
  }

  static async certificate(
    id: number,
    classResultShortName: string,
    personId: number,
    t: (key: string) => string
  ) {
    return axiosInstance
      .get(
        `${resultListUrl}/${id}/certificate?personId=${personId}&classResultShortName=${classResultShortName}`,
        { responseType: 'blob' }
      )
      .then((response) => {
        console.log(prettyPrint(response))
        // Extrahieren des Dateinamens aus dem Content-Disposition-Header
        const contentDisposition = response.headers['content-disposition']
        let filename = 'download.pdf' // Standard-Dateiname, falls nichts im Header gefunden wird
        if (contentDisposition) {
          const filenameMatch = contentDisposition.match(/filename="?(.+)"?/)
          if (filenameMatch.length === 2) {
            filename = filenameMatch[1]
          }
        }
        // Erstellen einer URL aus dem Blob
        const fileURL = window.URL.createObjectURL(new Blob([response.data]))
        // Erstellen eines temporären <a>-Elements zum Download
        const fileLink = document.createElement('a')
        fileLink.href = fileURL
        fileLink.setAttribute('download', filename) // Verwenden des extrahierten Dateinamens
        document.body.appendChild(fileLink)
        fileLink.click()
        document.body.removeChild(fileLink)
        window.URL.revokeObjectURL(fileURL)
      })
      .catch((error) => {
        handleApiError(error, t)
        return null
      })
  }
}

export const eventService = new EventService()
