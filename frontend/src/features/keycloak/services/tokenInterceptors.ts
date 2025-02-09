import type { ApiResponse } from '@/features/keycloak/model/apiResponse'
import { getErrorStore } from '@/features/common/stores/getErrorStore'
import axiosInstance from '@/features/keycloak/services/api'
import { getApiResponse } from '@/features/keycloak/services/apiResponseFunctions'
import { i18n } from '@/i18n'
import { errorToastDisplayDuration, sameErrorTimeout } from '@/utils/constants'
import { getDetail, getMessage, handleApiError } from '@/utils/HandleError'
import { ToastEventBus } from 'primevue'

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

    const handleError = async (error: any) => {
        const t: (key: string, object?: any) => string = i18n.global.t

        const apiResponse: ApiResponse<unknown> | undefined = await getApiResponse(error.response)

        const errorMessage = apiResponse
            ? t(
                    `backend.${apiResponse.message.messageKey.key}`,
                    apiResponse.message.messageParameters,
                )
            : t('errors.reallyUnknownApiError')

        if (errorCache.has(errorMessage)) {
            return
        }

        errorCache.add(errorMessage)
        setTimeout(() => errorCache.delete(errorMessage), sameErrorTimeout)

        try {
            await handleApiError(error, t)
        }
        catch (reason: any) {
            ToastEventBus.emit('add', {
                severity: 'error',
                summary: getMessage(reason),
                detail: (await getDetail(reason, t)),
                life: errorToastDisplayDuration,
            })
            const errorStore = getErrorStore()
            errorStore.addError(reason)
        }
    }

    axiosInstance.interceptors.response.use(
        response => response,
        async (error) => {
            const originalConfig = error.config

            if (error.response?.status === 401 && !originalConfig._retry) {
                originalConfig._retry = true
                try {
                    if (store.refreshUserToken) {
                        console.log('TokenInterceptors.refreshUserToken')
                        await store.refreshUserToken()
                    }
                    if (store.authenticated) {
                        originalConfig.headers = originalConfig.headers ?? {}
                        originalConfig.headers['x-access-token'] = store.user.token
                        originalConfig.headers.Authorization = `Bearer ${store.user.token}`
                    }
                    return axiosInstance(originalConfig)
                }
                catch (_error) {
                    console.error('Refresh token failed', _error)
                    // TODO: Handle token refresh failure (e.g., redirect to login page)
                }
                finally {
                    originalConfig._retry = false
                }
            }

            await handleError(error)

            // need reject, otherwise vue query will register success instead of error
            return Promise.reject(error)
        },

    )
}

export default setup
