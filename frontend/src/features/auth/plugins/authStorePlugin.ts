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
        console.log('[BFF Auth Plugin] Initializing BFF authentication...')
        if (options.router) {
            options.router.isReady().then(() => {
                console.log('[BFF Auth Plugin] Router is ready, calling initAuth...')
                store.ensureAuthInitialized().then((authenticated) => {
                    console.log('[BFF Auth Plugin] initAuth completed. Authenticated:', authenticated)
                    console.log('[BFF Auth Plugin] Store state:', {
                        authenticated: store.authenticated,
                        isAuthenticated: store.isAuthenticated,
                        isAdmin: store.isAdmin,
                        user: store.user,
                    })

                    // Handle post-login redirect
                    if (authenticated) {
                        const { path } = bffAuthService.getPostLoginRedirect()
                        console.log('[BFF Auth Plugin] Post-login redirect path:', path)
                        if (path && path !== '/' && path !== window.location.pathname) {
                            // Navigate to the stored redirect path
                            console.log('[BFF Auth Plugin] Navigating to:', path)
                            options.router!.push(path).catch((error) => {
                                console.error('Failed to navigate to post-login redirect:', error)
                            })
                        }
                    }
                })
            })
        }
        else {
            // Fallback if no router provided
            console.log('[BFF Auth Plugin] No router provided, calling initAuth directly...')
            store.ensureAuthInitialized()
        }
    },
}

export default authStorePlugin
