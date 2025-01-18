import { AxiosError } from 'axios'
import { getApiResponse } from '@/features/keycloak/services/apiResponseFunctions'

export class BackendError extends Error {
    public readonly baseError: Error
    constructor(name: string, baseError: Error, message?: string) {
        super(message || baseError.name)
        this.name = name
        this.stack = baseError.stack
        this.baseError = baseError
        // Workaround für die Vererbung in TypeScript (wichtig bei `Error`):
        Object.setPrototypeOf(this, new.target.prototype)
    }
}

class NetworkErrorException extends Error {
    public readonly details?: string
    public readonly baseError: Error
    constructor(name: string, baseError: Error, message?: string, details?: string) {
        super(message || baseError.name)
        this.name = name
        this.stack = baseError.stack
        this.baseError = baseError
        this.details = details
        // Workaround für die Vererbung in TypeScript (wichtig bei `Error`):
        Object.setPrototypeOf(this, new.target.prototype)
    }
}

export function handleApiError(error: unknown, t: (key: string, object?: any) => string) {
    if (error instanceof AxiosError) {
        const name = error.name
        if (error.response) {
            const apiResponse = getApiResponse(error.response)
            const message = apiResponse
                ? t(`backend.${apiResponse.message.messageKey.key}`, apiResponse.message.messageParameters)
                : error.message
            throw new BackendError(t('labels.error'), error, t('errors.internalServerError', { message }))
        }
        else if (error.request) {
            // no response
            if (error.code === 'ERR_NETWORK') {
                throw new NetworkErrorException(t('errors.networkError'), error)
            }
            else {
                throw new Error(
                    t('errors.noResponseError', {
                        name,
                        message: error.message,
                        code: error.code,
                    }),
                )
            }
        }
    }
    else {
        throw error instanceof Error
            ? error
            : new Error(t('errors.unknownError', { name: '', message: '' }))
    }
}
