import { setActivePinia } from 'pinia'

import { pinia } from '@/main' // Importiere die Pinia-Instanz
import { useMessageDetailStore } from '@/features/common/stores/useMessageDetailStore'

/**
 * Zugriff auf den MessageDetailStore außerhalb von `setup`.
 * @returns MessageDetailStore
 */
export function getMessageDetailStore() {
    if (!pinia) {
        throw new Error('Pinia ist nicht initialisiert.')
    }
    setActivePinia(pinia) // Setze die aktive Pinia-Instanz
    return useMessageDetailStore()
}
