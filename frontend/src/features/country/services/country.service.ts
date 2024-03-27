import type { Country } from '@/features/country/models/country'
import { GenericService } from '@/features/generic/services/GenericService'

const countryUrl: string = '/country'

export class CountryService extends GenericService<Country> {
    constructor() {
        super(countryUrl)
    }
}

export const countryService = new CountryService()
