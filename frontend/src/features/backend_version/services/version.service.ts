import { useQuery } from '@tanstack/vue-query'
import axiosInstance from '@/features/auth/services/api'

/**
 * Fetch backend version from API.
 * The /version endpoint returns a plain string, not an ApiResponse wrapper.
 *
 * @returns Promise with version string
 */
async function fetchBackendVersion(): Promise<string> {
    const response = await axiosInstance.get<string>('/version')
    return response.data
}

/**
 * Tanstack Query composable for backend version.
 *
 * Features:
 * - Long stale time (version rarely changes)
 * - Cache for 1 hour
 * - Retry on failure
 *
 * @returns Tanstack Query result
 */
export function useBackendVersion() {
    return useQuery({
        queryKey: ['backend', 'version'],
        queryFn: fetchBackendVersion,
        staleTime: 30 * 60 * 1000, // 30 minutes
        gcTime: 60 * 60 * 1000, // 1 hour
        retry: 2,
    })
}
