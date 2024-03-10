import type { EventKey } from '@/features/event/model/event_key'
import type { MediaKey } from '@/features/media/model/media_key'

export interface Certificate {
  id: number
  name: string
  event: EventKey | null
  layoutDescription: string
  blankCertificate: MediaKey | null
}
