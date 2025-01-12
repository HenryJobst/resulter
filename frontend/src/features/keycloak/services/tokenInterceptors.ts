import { ToastEventBus } from 'primevue'
import axiosInstance from '@/features/keycloak/services/api'
import KeycloakService from '@/features/keycloak/services/keycloak'
import { handleApiError } from '@/utils/HandleError'
import { sameErrorTimeout } from '@/utils/constants'
import { i18n } from '@/i18n'

function setup(store: any) {
    axiosInstance.interceptors.request.use(
        (config) => {
            if (store.authenticated) {
                config.headers = config.headers ?? {}
                config.headers['x-access-token'] = store.user.token
            }
            return config
        },
        error => Promise.reject(error),
    )

    const errorCache = new Set<string>()

    const handleError = (error: unknown) => {
        const t: (key: string, object?: any) => string = i18n.global.t
        const errorMessage
            = error instanceof Error ? error.message : t('errors.reallyUnknownApiError')

        if (errorCache.has(errorMessage)) {
            return
        }

        errorCache.add(errorMessage)
        setTimeout(() => errorCache.delete(errorMessage), sameErrorTimeout)

        try {
            handleApiError(error, t)
        }
        catch (reason: any) {
            ToastEventBus.emit('add', {
                severity: 'error',
                summary: reason.name,
                detail: reason.stack,
            })
        }
    }

    axiosInstance.interceptors.response.use(
        response => response,
        async (error) => {
            const originalConfig = error.config

            if (error.response?.status === 401 && !originalConfig._retry) {
                originalConfig._retry = true
                try {
                    await store.refreshUserToken()
                    originalConfig.headers = originalConfig.headers ?? {}
                    originalConfig.headers['x-access-token'] = store.user.token
                    return axiosInstance(originalConfig)
                }
                catch (_error) {
                    console.error('Refresh token failed', _error)
                    await KeycloakService.callLogin(originalConfig.url, originalConfig.locale)
                }
            }

            handleError(error)

            return Promise.reject(error)
        },
    )
}

export default setup
