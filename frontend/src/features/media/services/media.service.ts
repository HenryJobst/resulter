import axiosInstance from '@/features/keycloak/services/api'
import { handleApiError } from '@/utils/HandleError'
import { GenericService } from '@/features/generic/services/GenericService'
import type { Media } from '@/features/media/model/media'

const mediaUrl: string = '/media'

export class MediaService extends GenericService<Media> {
  constructor() {
    super(mediaUrl)
  }

  static async upload(formData: FormData, t: (key: string) => string) {
    return axiosInstance
      .post(mediaUrl + '/upload', formData, {
        headers: {
          'Content-Type': 'multipart/form-data'
        }
      })
      .then((response) => response.data)
      .catch((error) => {
        handleApiError(error, t)
        return null
      })
  }
}

export const mediaService = new MediaService()
