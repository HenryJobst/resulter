import type { Pinia } from 'pinia'
import type { App } from 'vue'
import KeycloakService from '@/features/keycloak/services/keycloak'
import setupInterceptors from '@/features/keycloak/services/tokenInterceptors'
import { useAuthStore } from '@/features/keycloak/store/auth.store'

// Definieren Sie eine Schnittstelle für die Plugin-Optionen, falls erforderlich
export interface AuthStorePluginOptions {
    pinia: Pinia
}

const authStorePlugin = {
    install(app: App, options: AuthStorePluginOptions): void {
        const store = useAuthStore(options.pinia)

        // Globalen Store zu Vue's globalProperties hinzufügen
        app.config.globalProperties.$store = store

        // Keycloak-Benutzerdaten in den Store laden
        KeycloakService.callInitStore(store)

        setupInterceptors(store)
    },
}

export default authStorePlugin
