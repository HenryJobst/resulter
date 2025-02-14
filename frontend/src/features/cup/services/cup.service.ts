import type { Cup } from '@/features/cup/model/cup'
import type { CupDetailed } from '@/features/cup/model/cup_detailed'
import type { CupType } from '@/features/cup/model/cuptype'
import { GenericService } from '@/features/generic/services/GenericService'
import axiosInstance from '@/features/keycloak/services/api'

const cupUrl: string = '/cup'
const cupTypeUrl: string = '/cup_types'

export class CupService extends GenericService<Cup> {
    constructor() {
        super(cupUrl)
    }

    static async getCupTypes(_t: (key: string) => string): Promise<CupType[] | null> {
        return await axiosInstance
            .get<CupType[]>(cupTypeUrl)
            .then(response => response.data)
    }

    static async getResultsById(id: string, _t: (key: string) => string): Promise<CupDetailed> {
        return await axiosInstance
            .get(`${cupUrl}/${id}/results`)
            .then(response => response.data)
    }

    static async calculate(cup_id: string, _t: (key: string) => string) {
        return axiosInstance
            .put(`${cupUrl}/${cup_id}/calculate`)
            .then(response => response.data)
    }
}

export const cupService = new CupService()
