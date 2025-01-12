import { AxiosError } from 'axios'

class NotFoundException extends Error {
    constructor(message: string) {
        super(message) // Übergabe der Fehlermeldung an die Basisklasse
        // Workaround für die Vererbung in TypeScript (wichtig bei `Error`):
        Object.setPrototypeOf(this, new.target.prototype)
    }
}

class BadRequestException extends Error {
    constructor(message: string) {
        super(message) // Übergabe der Fehlermeldung an die Basisklasse
        // Workaround für die Vererbung in TypeScript (wichtig bei `Error`):
        Object.setPrototypeOf(this, new.target.prototype)
    }
}

class UnauthorizedException extends Error {
    constructor(message: string) {
        super(message) // Übergabe der Fehlermeldung an die Basisklasse
        // Workaround für die Vererbung in TypeScript (wichtig bei `Error`):
        Object.setPrototypeOf(this, new.target.prototype)
    }
}

class ForbiddenException extends Error {
    constructor(message: string) {
        super(message) // Übergabe der Fehlermeldung an die Basisklasse
        // Workaround für die Vererbung in TypeScript (wichtig bei `Error`):
        Object.setPrototypeOf(this, new.target.prototype)
    }
}

class ConflictException extends Error {
    constructor(message: string) {
        super(message) // Übergabe der Fehlermeldung an die Basisklasse
        // Workaround für die Vererbung in TypeScript (wichtig bei `Error`):
        Object.setPrototypeOf(this, new.target.prototype)
    }
}

class InternalServerErrorException extends Error {
    constructor(message: string) {
        super(message) // Übergabe der Fehlermeldung an die Basisklasse
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
        if (error.response) {
            // Got response from server
            switch (error.response.status) {
                case 400:
                    throw new BadRequestException(
                        t('errors.badRequest', { name: error.name, message: error.message }),
                    )
                case 401:
                    throw new UnauthorizedException(
                        t('errors.unauthorized', { name: error.name, message: error.message }),
                    )
                case 403:
                    throw new ForbiddenException(
                        t('errors.forbidden', { name: error.name, message: error.message }),
                    )
                case 404:
                    throw new NotFoundException(
                        t('errors.notFound', { name: error.name, message: error.message }),
                    )
                case 409:
                    throw new ConflictException(
                        t('errors.conflict', { name: error.name, message: error.message }),
                    )
                case 500:
                    throw new InternalServerErrorException(
                        t('errors.internalServerError', { name: error.name, message: error.message }),
                    )
                default:
                    if (error.code)
                        console.log(error.code)

                    throw new Error(t('errors.unknownApiError', { name: error.name, message: error.message }))
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
