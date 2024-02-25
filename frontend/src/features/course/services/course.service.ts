import type { Course } from '@/features/course/model/course'
import { GenericService } from '@/features/generic/services/GenericService'

const courseUrl: string = '/course'

export class CourseService extends GenericService<Course> {
  constructor() {
    super(courseUrl)
  }
}

export const courseService = new CourseService()
