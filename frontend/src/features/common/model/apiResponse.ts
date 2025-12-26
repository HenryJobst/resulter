export interface LocalizableString {
    messageKey: {
        key: string
    }
    messageParameters?: Record<string, any>
}

export interface ApiResponse<T> {
    success: boolean
    message: LocalizableString
    data?: T
    errors?: string[]
    errorCode?: number
    timestamp?: number
    path?: string
}
