import { setActivePinia } from 'pinia'
import { pinia } from '@/main'
import { useErrorStore } from '@/features/common/stores/useErrorStore'

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
