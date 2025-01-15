import { AxiosError } from 'axios'
import { getApiResponse } from '@/features/keycloak/services/apiResponseFunctions'

export class BackendError extends Error {
    constructor(message: string) {
        super(message) // Übergabe der Fehlermeldung an die Basisklasse
        // Workaround für die Vererbung in TypeScript (wichtig bei `Error`):
        Object.setPrototypeOf(this, new.target.prototype)
    }
}

class NotFoundException extends BackendError {
    constructor(name: string, message: string) {
        super(message) // Übergabe der Fehlermeldung an die Basisklasse
        this.name = name
        // Workaround für die Vererbung in TypeScript (wichtig bei `Error`):
        Object.setPrototypeOf(this, new.target.prototype)
    }
}

class BadRequestException extends BackendError {
    constructor(message: string) {
        super(message) // Übergabe der Fehlermeldung an die Basisklasse
        // Workaround für die Vererbung in TypeScript (wichtig bei `Error`):
        Object.setPrototypeOf(this, new.target.prototype)
    }
}

class UnauthorizedException extends BackendError {
    constructor(message: string) {
        super(message) // Übergabe der Fehlermeldung an die Basisklasse
        // Workaround für die Vererbung in TypeScript (wichtig bei `Error`):
        Object.setPrototypeOf(this, new.target.prototype)
    }
}

class ForbiddenException extends BackendError {
    constructor(message: string) {
        super(message) // Übergabe der Fehlermeldung an die Basisklasse
        // Workaround für die Vererbung in TypeScript (wichtig bei `Error`):
        Object.setPrototypeOf(this, new.target.prototype)
    }
}

class ConflictException extends BackendError {
    constructor(message: string) {
        super(message) // Übergabe der Fehlermeldung an die Basisklasse
        // Workaround für die Vererbung in TypeScript (wichtig bei `Error`):
        Object.setPrototypeOf(this, new.target.prototype)
    }
}

class InternalServerErrorException extends BackendError {
    constructor(name: string, message: string) {
        super(message) // Übergabe der Fehlermeldung an die Basisklasse
        this.name = name
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
            // Got response from server
            switch (error.response.status) {
                case 400:
                    throw new BadRequestException(
                        t('errors.badRequest', { name, message }),
                    )
                case 401:
                    throw new UnauthorizedException(
                        t('errors.unauthorized', { name, message }),
                    )
                case 403:
                    throw new ForbiddenException(
                        t('errors.forbidden', { name, message }),
                    )
                case 404:
                    throw new NotFoundException(t('labels.error'), t('errors.notFound', { message }))
                case 409:
                    throw new ConflictException(
                        t('errors.conflict', { name, message }),
                    )
                case 500:
                    throw new InternalServerErrorException(
                        t('labels.error'),
                        t('errors.internalServerError', { message }),
                    )
                default:
                    if (error.code) {
                        console.log(`Error code: ${error.code}`)
                    }
                    throw new Error(t('errors.unknownApiError', { name, message }))
            }
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
