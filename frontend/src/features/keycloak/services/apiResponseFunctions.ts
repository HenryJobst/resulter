import type { ApiResponse } from '@/features/keycloak/model/apiResponse'

function isApiResponse(data: any): data is ApiResponse<unknown> {
    return data && typeof data.success === 'boolean' && 'message' in data
}

export async function getApiResponse(response: any): Promise<ApiResponse<unknown> | undefined> {
    let apiResponse: ApiResponse<unknown> | undefined

    if (response?.data && isApiResponse(response.data)) {
        apiResponse = response.data as ApiResponse<unknown>
    }
    else if (response?.data instanceof Blob) {
        try {
            const jsonText = await response.data.text()
            const parsedData = JSON.parse(jsonText)

            if (isApiResponse(parsedData)) {
                apiResponse = parsedData as ApiResponse<unknown>
            }
        }
        catch (error) {
            console.error('Fehler beim Parsen des Blob:', error)
        }
    }

    return apiResponse
}
