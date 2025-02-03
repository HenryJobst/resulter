import { defineStore } from 'pinia'

// Typ für ein beliebiges Fehlerobjekt
export interface StoredError<T = unknown> {
    id: number // Eindeutige ID des gespeicherten Fehlers
    originalError: T // Das Original-Fehlerobjekt
    timestamp?: Date // Optionaler Zeitstempel
}

export const useErrorStore = defineStore('errorStore', {
    state: () => ({
        errors: [] as StoredError[], // Liste der gespeicherten Fehlerobjekte
    }),
    getters: {
        // Gibt alle gespeicherten Fehler zurück
        getErrors: (state): StoredError[] => state.errors,
        // Gibt die Anzahl der gespeicherten Fehler zurück
        errorCount: (state): number => state.errors.length,
        getError:
                state =>
                    (id: number): StoredError | undefined => {
                        return state.errors.find(error => error.id === id)
                    },
    },
    actions: {
        /**
         * Fügt ein neues Fehlerobjekt hinzu.
         * @param errorObj - Das Original-Fehlerobjekt, das gespeichert werden soll.
         */
        addError<T>(errorObj: T): void {
            const storedError: StoredError<T> = {
                id: Date.now(), // Eindeutige ID basierend auf der Zeit
                originalError: errorObj,
                timestamp: new Date(),
            }
            this.errors.push(storedError)
        },
        /**
         * Entfernt einen Fehler basierend auf seiner ID.
         * @param id - Die ID des zu entfernenden Fehlers.
         */
        removeError(id: number): void {
            this.errors = this.errors.filter(error => error.id !== id)
        },
        /**
         * Entfernt alle gespeicherten Fehler.
         */
        clearErrors(): void {
            this.errors = []
        },
    },
})
