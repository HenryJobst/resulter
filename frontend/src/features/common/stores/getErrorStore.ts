import { setActivePinia } from 'pinia'
import { useErrorStore } from '@/features/common/stores/useErrorStore'
import { pinia } from '@/main'

/**
 * Zugriff auf den ErrorStore außerhalb von `setup`.
 * @returns Der ErrorStore
 */
export function getErrorStore() {
    if (!pinia) {
        throw new Error('Pinia ist nicht initialisiert.')
    }
    setActivePinia(pinia) // Setze die aktive Pinia-Instanz
    return useErrorStore()
}
