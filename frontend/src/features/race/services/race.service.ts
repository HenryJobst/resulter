import type { Race } from '@/features/race/model/race'
import { GenericService } from '@/features/generic/services/GenericService'

const raceUrl: string = '/race'

export class RaceService extends GenericService<Race> {
  constructor() {
    super(raceUrl)
  }
}

export const raceService = new RaceService()
