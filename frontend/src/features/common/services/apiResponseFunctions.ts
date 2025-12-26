import type { AxiosResponse } from 'axios'
import type { ApiResponse } from '../model/apiResponse'

export async function getApiResponse<T>(response: AxiosResponse): Promise<ApiResponse<T> | undefined> {
    try {
        if (response.data) {
            return response.data as ApiResponse<T>
        }
    }
    catch (error) {
        console.error('Error parsing API response:', error)
    }
    return undefined
}
