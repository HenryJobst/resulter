import axiosInstance from '@/features/keycloak/services/api'
import type { Person } from '@/features/person/model/person'
import { handleApiError } from '@/utils/HandleError'
import type { Gender } from '@/features/person/model/gender'
import { GenericService } from '@/features/generic/services/GenericService'

const personUrl: string = '/person'
const genderUrl: string = '/gender'

export class PersonService extends GenericService<Person> {
    constructor() {
        super(personUrl)
    }

    static async getGender(t: (key: string) => string): Promise<Gender[] | null> {
        return await axiosInstance
            .get<Gender[]>(genderUrl)
            .then(response => response.data)
            .catch((error) => {
                handleApiError(error, t)
                return null
            })
    }

    static async getPersonDoubles(
        id: number,
        t: (key: string) => string,
    ): Promise<Person[] | null> {
        if (id === 0) {
            return null
        }
        const personDoublesUrl = `${personUrl}/${id.toString()}/doubles`
        return await axiosInstance
            .get<Person[]>(personDoublesUrl)
            .then(response => response.data)
            .catch((error) => {
                handleApiError(error, t)
                return null
            })
    }
}

export const personService = new PersonService()
