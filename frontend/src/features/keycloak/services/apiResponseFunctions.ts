import type { ApiResponse } from '@/features/keycloak/model/apiResponse'

function isApiResponse(data: any): data is ApiResponse<unknown> {
    return data && typeof data.success === 'boolean' && 'message' in data
}

export function getApiResponse(response: any) {
    let apiResponse: ApiResponse<unknown> | undefined
    if (response?.data && isApiResponse(response.data)) {
        apiResponse = response.data as ApiResponse<unknown>
    }
    return apiResponse
}
