import axiosInstance from '@/features/keycloak/services/api'
import type { Organisation } from '@/features/organisation/model/organisation'
import type { OrganisationType } from '@/features/organisation/model/organisationtype'
import { GenericService } from '@/features/generic/services/GenericService'

const organisationUrl: string = '/organisation'
const organisationTypeUrl: string = '/organisation_types'

export class OrganisationService extends GenericService<Organisation> {
    constructor() {
        super(organisationUrl)
    }

    static async getOrganisationTypes(
        _t: (key: string) => string,
    ): Promise<OrganisationType[] | null> {
        return await axiosInstance
            .get<OrganisationType[]>(organisationTypeUrl)
            .then(response => response.data)
    }
}

export const organisationService = new OrganisationService()
