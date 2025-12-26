import type { Gender } from '@/features/person/model/gender'
import type { Person } from '@/features/person/model/person'
import axiosInstance from '@/features/auth/services/api'
import { GenericService } from '@/features/generic/services/GenericService'

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

class DuplicatePersonService extends GenericService<Person> {
    constructor() {
        super(personUrl)
    }

    protected getExtraParams(): Record<string, string | number | boolean> {
        return { duplicates: true }
    }
}

export const personService = new PersonService()
export const duplicatePersonService = new DuplicatePersonService()
