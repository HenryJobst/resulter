import { AxiosError } from 'axios'

interface LocalizableString {
    messageKey: {
        key: string
    }
    messageParameters?: Record<string, any>
}

interface ApiResponse<T> {
    message: LocalizableString
    errors?: string[]
    data?: T
}

interface ProblemDetailLike {
    title?: string
    detail?: string
    code?: string
    params?: Record<string, any>
    errors?: string[]
}

function extractErrorPayload<T>(responseData: unknown): ApiResponse<T> | ProblemDetailLike | undefined {
    if (!responseData || typeof responseData !== 'object') {
        return undefined
    }
    return responseData as ApiResponse<T> | ProblemDetailLike
}

function isApiResponsePayload<T>(payload: ApiResponse<T> | ProblemDetailLike): payload is ApiResponse<T> {
    return (
        'message' in payload
        && typeof payload.message === 'object'
        && payload.message !== null
        && 'messageKey' in payload.message
    )
}

function translateBackendCode(code: string | undefined, params: Record<string, any> | undefined, t: (key: string, object?: any) => string): string {
    if (!code) {
        return ''
    }
    return t(`backend.${code}`, params)
}

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
            const payload = extractErrorPayload(error.response.data)
            const message = payload
                ? (
                        isApiResponsePayload(payload)
                            ? translateBackendCode(payload.message.messageKey.key, payload.message.messageParameters, t)
                            : translateBackendCode(payload.code, payload.params, t) || payload.detail || payload.title || error.message
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
            const payload = extractErrorPayload(error.baseError.response.data)
            if (payload) {
                if (payload.errors && payload.errors.length > 0) {
                    return payload.errors.join(', ')
                }
                if (isApiResponsePayload(payload)) {
                    return t(
                        `backend.${payload.message.messageKey.key}`,
                        payload.message.messageParameters,
                    )
                }
                return translateBackendCode(payload.code, payload.params, t) || payload.detail || payload.title || error.message
            }
        }
    }
    return error.stack
}
