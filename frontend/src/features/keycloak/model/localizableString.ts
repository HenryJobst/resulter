import type { MessageKey } from '@/features/keycloak/model/messageKey'

export interface LocalizableString {
    messageKey: MessageKey
    messageParameters?: any
}
