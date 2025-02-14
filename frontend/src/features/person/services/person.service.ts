import type { Gender } from '@/features/person/model/gender'
import type { Person } from '@/features/person/model/person'
import { GenericService } from '@/features/generic/services/GenericService'
import axiosInstance from '@/features/keycloak/services/api'

const personUrl: string = '/person'
const genderUrl: string = '/gender'

export class PersonService extends GenericService<Person> {
    constructor() {
        super(personUrl)
    }

    static async getGender(_t: (key: string) => string): Promise<Gender[] | null> {
        return await axiosInstance
            .get<Gender[]>(genderUrl)
            .then(response => response.data)
    }

    static async getPersonDoubles(
        id: number,
        _t: (key: string) => string,
    ): Promise<Person[] | null> {
        if (id === 0) {
            return null
        }
        const personDoublesUrl = `${personUrl}/${id.toString()}/doubles`
        return await axiosInstance
            .get<Person[]>(personDoublesUrl)
            .then(response => response.data)
    }

    static async merge(keepId: number, removeId: number, _t: (key: string) => string) {
        const mergeUrl = `${personUrl}/${keepId.toString()}/merge`
        return await axiosInstance
            .post(mergeUrl, removeId)
            .then(response => response.data)
    }
}

export const personService = new PersonService()
