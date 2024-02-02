import axiosInstance from '@/features/keycloak/services/api'
import type { Course } from '@/features/course/model/course'
import { handleApiError } from '@/utils/HandleError'

const url: string = import.meta.env.VITE_API_ENDPOINT + 'course'

export class CourseService {
  static async getAll(t: (key: string) => string): Promise<Course[] | null> {
    return await axiosInstance
      .get<Course[]>(url)
      .then((response) => response.data)
      .catch((error) => {
        handleApiError(error, t)
        return null
      })
  }
}
