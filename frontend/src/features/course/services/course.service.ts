import axiosInstance from '@/features/keycloak/services/api'
import type { Course } from '@/features/course/model/course'
import { handleApiError } from '@/utils/HandleError'
import { GenericService } from '@/features/generic/services/GenericService'

const courseUrl: string = '/course'

export class CourseService extends GenericService<Course> {
  constructor() {
    super(courseUrl)
  }

  static async getAll(t: (key: string) => string): Promise<Course[] | null> {
    return await axiosInstance
      .get<Course[]>(courseUrl)
      .then((response) => response.data)
      .catch((error) => {
        handleApiError(error, t)
        return null
      })
  }
}

export const courseService = new CourseService()
