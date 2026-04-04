import type { Pinia } from 'pinia'
import type { App } from 'vue'
import type { Router } from 'vue-router'
import { bffAuthService } from '../services/bffAuthService'
import { useAuthStore } from '../store/auth.store'

// Define interface for plugin options
export interface AuthStorePluginOptions {
    pinia: Pinia
    router?: Router
}

const authStorePlugin = {
    install(app: App, options: AuthStorePluginOptions): void {
        const store = useAuthStore(options.pinia)

        // Add global store to Vue's globalProperties
        app.config.globalProperties.$store = store

        // BFF mode: initialize auth when router is ready
        if (options.router) {
            options.router.isReady().then(() => {
                store.ensureAuthInitialized().then((authenticated) => {
                    // Handle post-login redirect
                    if (authenticated) {
                        const { path } = bffAuthService.getPostLoginRedirect()
                        if (path && path !== '/' && path !== window.location.pathname) {
                            options.router!.push(path).catch((error) => {
                                console.error('Failed to navigate to post-login redirect:', error)
                            })
                        }
                    }
                })
            })
        }
        else {
            store.ensureAuthInitialized()
        }
    },
}

export default authStorePlugin
