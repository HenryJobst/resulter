export interface ApiResponse<T> {
    success: boolean // Indicates whether the request was successful or not
    message: string // Descriptive message about the result
    data: T | null // The actual response data of type T (nullable for error cases)
    errors: string[] // List of errors if the request failed
    errorCode: number // Integer code representing the error type
    timestamp: number // Timestamp of when the response was generated
    path: string // URL path of the request for debugging purposes
}
