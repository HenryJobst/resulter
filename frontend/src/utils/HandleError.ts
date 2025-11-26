import type { ApiResponse } from '@/features/keycloak/model/apiResponse'
import { AxiosError } from 'axios'
import { getApiResponse } from '@/features/keycloak/services/apiResponseFunctions'

export class BackendException extends Error {
    public readonly baseError: Error
    constructor(baseError: Error, message?: string) {
        super(message || baseError.name)
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

export async function handleApiError(error: unknown, t: (key: string, object?: any) => string) {
    if (error instanceof AxiosError) {
        if (error.response) {
            const apiResponse = await getApiResponse(error.response)
            const message = apiResponse
                ? t(
                        `backend.${apiResponse.message.messageKey.key}`,
                        apiResponse.message.messageParameters,
                    )
                : error.message
            throw new BackendException(error, message)
        }
        else if (error.request) {
            // no response
            if (error.code === 'ERR_NETWORK') {
                throw new NetworkErrorException(t('errors.networkError'), error)
            }
            else {
                throw new Error(
                    t('errors.noResponseError', {
                        name: error.name,
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

export function getMessage(error: any): string {
    return error instanceof BackendException ? error.message : (error.name ?? error.message)
}

export async function getDetail(error: any, t: (key: string, object?: any) => string): Promise<string> {
    if (error instanceof BackendException) {
        if (error.baseError instanceof AxiosError && error.baseError.response) {
            const apiResponse: ApiResponse<unknown> | undefined = await getApiResponse(
                error.baseError.response,
            )
            if (apiResponse) {
                if (apiResponse.errors) {
                    return apiResponse!.errors.join(', ')
                }
                else {
                    return t(
                        `backend.${apiResponse!.message.messageKey.key}`,
                        apiResponse!.message.messageParameters,
                    )
                }
            }
        }
    }
    return error.stack
}
