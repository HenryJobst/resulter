import { AxiosError } from 'axios'

class NotFoundException extends Error {
    /* ... */
}

class BadRequestException extends Error {
    /* ... */
}

class UnauthorizedException extends Error {
    /* ... */
}

class ForbiddenException extends Error {
    /* ... */
}

class ConflictException extends Error {
    /* ... */
}

class InternalServerErrorException extends Error {
    /* ... */
}

export function handleApiError(error: unknown, t: (key: string, object: any) => string) {
    if (error instanceof AxiosError) {
        console.log(error)
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
            console.log(error.toJSON())
            throw new Error(
                t('errors.noResponseError', {
                    name: error.name,
                    message: error.message,
                    code: error.code,
                }),
            )
        }
    }
    else {
        throw error instanceof Error
            ? error
            : new Error(t('errors.unknownError', { name: '', message: '' }))
    }
}
