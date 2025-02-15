import type { Media } from '@/features/media/model/media'
import { GenericService } from '@/features/generic/services/GenericService'
import axiosInstance from '@/features/keycloak/services/api'

const mediaUrl: string = '/media'

export class MediaService extends GenericService<Media> {
    constructor() {
        super(mediaUrl)
    }

    static async upload(formData: FormData, _t: (key: string) => string) {
        return axiosInstance
            .post(`${mediaUrl}/upload`, formData, {
                headers: {
                    'Content-Type': 'multipart/form-data',
                },
            })
            .then(response => response.data)
    }
}

export const mediaService = new MediaService()
