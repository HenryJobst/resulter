import type { User } from '@/features/keycloak/model/user'

export interface AuthStoreState {
  authenticated: boolean
  user: User
}
