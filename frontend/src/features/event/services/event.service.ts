import type { Certificate } from '@/features/certificate/model/certificate'
import type { CupScoreList } from '@/features/event/model/cup_score_list'
import type { EventCertificateStats } from '@/features/event/model/event_certificate_stats'
import type { EventResults } from '@/features/event/model/event_results'
import type { EventStatus } from '@/features/event/model/event_status'
import type { ResultList } from '@/features/event/model/result_list'
import type { SportEvent } from '@/features/event/model/sportEvent'
import type { RestPageResult } from '@/features/generic/models/rest_page_result'
import type { TableSettings } from '@/features/generic/models/table_settings'
import { prettyPrint } from '@base2/pretty-print-object'
import { GenericService } from '@/features/generic/services/GenericService'
import axiosInstance from '@/features/keycloak/services/api'

const eventUrl: string = '/event'
const resultListUrl: string = '/result_list'
const eventStatusUrl: string = '/event_status'

export class EventService extends GenericService<SportEvent> {
    constructor() {
        super(eventUrl)
    }

    async getAll(
        t: (key: string) => string,
        tableSettings?: TableSettings,
    ): Promise<RestPageResult<SportEvent> | null> {
        return await super.getAll(t, tableSettings).then((response) => {
            if (response) {
                const result = response
                result.content = result.content.map((element) => {
                    if (element.startTime)
                        element.startTime = new Date(element.startTime)

                    return element
                })
                return result
            }
            return null
        })
    }

    async getAllUnpaged(t: (key: string) => string): Promise<SportEvent[] | null> {
        return await super.getAllUnpaged(t).then((response) => {
            if (response) {
                let result = response
                result = result.map((element) => {
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

    static async calculate(result_list_id: number, _t: (key: string) => string) {
        return axiosInstance
            .put(`${resultListUrl}/${result_list_id}/calculate`)
            .then(response => response.data)
    }

    static async getEventStatus(_t: (key: string) => string): Promise<EventStatus[] | null> {
        return await axiosInstance
            .get<EventStatus[]>(eventStatusUrl)
            .then(response => response.data)
    }

    static async getResultsById(
        id: string,
        _t: (key: string) => string,
    ): Promise<EventResults | null> {
        return await axiosInstance
            .get(`${eventUrl}/${id}/results`)
            .then((response) => {
                if (response) {
                    response.data.resultLists = response.data.resultLists.map(
                        (resultList: ResultList) => {
                            if (
                                resultList.createTime
                                && typeof resultList.createTime === 'string'
                            ) {
                                // Entfernen des Zeitzone-Identifikators, da dieser nicht von Date.parse() unterstützt wird
                                const dateStringWithoutTimezone
                                    = resultList.createTime.split('[')[0]
                                resultList.createTime = new Date(dateStringWithoutTimezone)
                            }
                            return resultList
                        },
                    )
                    return response.data
                }
                return null
            })
    }

    static async upload(formData: FormData, _t: (key: string) => string) {
        return axiosInstance
            .post('/upload', formData, {
                headers: {
                    'Content-Type': 'multipart/form-data',
                },
            })
            .then(response => response.data)
    }

    static async certificate(
        id: number,
        classResultShortName: string,
        personId: number,
        _t: (key: string) => string,
    ) {
        return axiosInstance
            .get(
                `${resultListUrl}/${id}/certificate?personId=${personId}&classResultShortName=${classResultShortName}`,
                { responseType: 'blob' },
            )
            .then((response) => {
                console.log(prettyPrint(response))
                // Extrahieren des Dateinamens aus dem Content-Disposition-Header
                const contentDisposition = response.headers['content-disposition']
                let filename = 'download.pdf' // Standard-Dateiname, falls nichts im Header gefunden wird
                if (contentDisposition) {
                    // Improved regex to handle different possible formats of Content-Disposition
                    const filenameMatch = contentDisposition.match(
                        /filename[^;=\n]*=((['"]).*?\2|[^;\n]*)/,
                    )
                    if (filenameMatch && filenameMatch.length > 1)
                        filename = filenameMatch[1].replace(/['"]/g, '') // Remove any quotes
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
    }

    static async getCertificate(certificate: Certificate | undefined, _t: (key: string) => string) {
        if (!certificate || !certificate.event)
            return null

        return axiosInstance
            .put(`${eventUrl}/${certificate.event?.id}/certificate`, certificate, {
                responseType: 'blob',
            })
            .then((response) => {
                return window.URL.createObjectURL(
                    new Blob([response.data], { type: 'application/pdf' }),
                )
            })
    }

    static async getCertificateStats(
        id: number | undefined,
        _t: (key: string) => string,
    ): Promise<EventCertificateStats | null> {
        if (!id) {
            return null
        }
        return axiosInstance
            .get(`${eventUrl}/${id}/certificate_stats`)
            .then(response => response.data)
    }

    static async removeEventCertificateStat(id: number, _t: (key: string) => string) {
        if (!id)
            return null
        return axiosInstance
            .delete(`/event_certificate_stat/${id}`)
            .then(response => response.data)
    }

    static async getCertificateSchema(_t: (key: string) => string) {
        return axiosInstance
            .get('/certificate_schema')
            .then(response => response.data)
    }

    static async getCupScores(
        id: number | undefined,
        _t: (key: string) => string,
    ): Promise<CupScoreList[]> {
        if (!id) {
            return []
        }
        return axiosInstance
            .get(`${resultListUrl}/${id}/cup_score_lists`)
            .then(response => response.data)
    }
}

export const eventService = new EventService()
