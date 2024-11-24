import axiosInstance from '@/features/keycloak/services/api'
import type { Cup } from '@/features/cup/model/cup'
import { handleApiError } from '@/utils/HandleError'
import type { CupResults } from '@/features/cup/model/cup_results'
import type { CupType } from '@/features/cup/model/cuptype'
import { GenericService } from '@/features/generic/services/GenericService'

const cupUrl: string = '/cup'
const cupTypeUrl: string = '/cup_types'

export class CupService extends GenericService<Cup> {
    constructor() {
        super(cupUrl)
    }

    static async getCupTypes(t: (key: string) => string): Promise<CupType[] | null> {
        return await axiosInstance
            .get<CupType[]>(cupTypeUrl)
            .then(response => response.data)
            .catch((error) => {
                handleApiError(error, t)
                return null
            })
    }

    static async getResultsById(id: string, t: (key: string) => string): Promise<CupResults> {
        return await axiosInstance
            .get(`${cupUrl}/${id}/results`)
            .then(response => response.data)
            .catch((error) => {
                handleApiError(error, t)
                return null
            })
    }

    static async calculate(cup_id: number, t: (key: string) => string) {
        return axiosInstance
            .put(`${cupUrl}/${cup_id}/calculate`)
            .then(response => response.data)
            .catch((error) => {
                handleApiError(error, t)
                return null
            })
    }
}

export const cupService = new CupService()
