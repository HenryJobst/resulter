import type { Media } from '@/features/media/model/media'
import axiosInstance from '@/features/auth/services/api'
import { GenericService } from '@/features/generic/services/GenericService'

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

    static async download(id: number, fileName: string) {
        const response = await axiosInstance.get(`${mediaUrl}/${id}/download`, { responseType: 'blob' })
        const fileURL = window.URL.createObjectURL(response.data)
        const fileLink = document.createElement('a')
        fileLink.href = fileURL
        fileLink.setAttribute('download', fileName)
        document.body.appendChild(fileLink)
        fileLink.click()
        document.body.removeChild(fileLink)
        window.URL.revokeObjectURL(fileURL)
    }
}

export const mediaService = new MediaService()
