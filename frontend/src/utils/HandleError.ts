import { AxiosError } from 'axios'

class NotFoundException extends Error {
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
    //console.log(error)
    switch (error.response?.status) {
      case 404:
        throw new NotFoundException(
          t('errors.notFound', { name: error.name, message: error.message })
        )
      case 401:
        throw new UnauthorizedException(
          t('errors.unauthorized', { name: error.name, message: error.message })
        )
      case 403:
        throw new ForbiddenException(
          t('errors.forbidden', { name: error.name, message: error.message })
        )
      case 409:
        throw new ConflictException(
          t('errors.conflict', { name: error.name, message: error.message })
        )
      case 500:
        throw new InternalServerErrorException(
          t('errors.internalServerError', { name: error.name, message: error.message })
        )
      default:
        throw new Error(t('errors.unknownApiError', { name: error.name, message: error.message }))
    }
  } else {
    throw error instanceof Error
      ? error
      : new Error(t('errors.unknownError', { name: '', message: '' }))
  }
}
