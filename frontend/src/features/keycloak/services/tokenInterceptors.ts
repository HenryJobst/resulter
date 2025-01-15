import { ToastEventBus } from 'primevue'
import axiosInstance from '@/features/keycloak/services/api'
import KeycloakService from '@/features/keycloak/services/keycloak'
import { BackendError, handleApiError } from '@/utils/HandleError'
import { sameErrorTimeout } from '@/utils/constants'
import { i18n } from '@/i18n'
import type { ApiResponse } from '@/features/keycloak/model/apiResponse'
import { getApiResponse } from '@/features/keycloak/services/apiResponseFunctions'

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

    const handleError = (error: any) => {
        const t: (key: string, object?: any) => string = i18n.global.t

        const apiResponse: ApiResponse<unknown> | undefined = getApiResponse(error.response)

        const errorMessage
            = apiResponse
                ? t(`backend.${apiResponse.message.messageKey.key}`, apiResponse.message.messageParameters)
                : t('errors.reallyUnknownApiError')

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
                summary: reason instanceof BackendError ? reason.message : reason.name,
                detail: reason.stack,
            })
        }
    }

    axiosInstance.interceptors.response.use(
        (response) => {
            const apiResponse = getApiResponse(response)
            if (apiResponse) {
                const t: (key: string, object?: any) => string = i18n.global.t
                if (!apiResponse.success) {
                    handleError(
                        new Error(
                            t(
                                apiResponse.message.messageKey.key,
                                apiResponse.message.messageParameters,
                            ),
                        ),
                    )
                }
            }
            return response
        },
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
