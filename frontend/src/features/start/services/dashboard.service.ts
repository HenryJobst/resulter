import type { DashboardStatistics } from '@/features/start/model/dashboard_statistics'
import { useQuery } from '@tanstack/vue-query'
import axiosInstance from '@/features/auth/services/api'

const dashboardUrl: string = '/dashboard'

/**
 * API Response wrapper from backend.
 */
interface ApiResponse<T> {
    success: boolean
    message: any
    data: T
    errors: string[] | null
    errorCode: number
    timestamp: number
    path: string
}

/**
 * Fetch dashboard statistics from API.
 *
 * @returns Promise with dashboard statistics
 */
async function fetchDashboardStatistics(): Promise<DashboardStatistics> {
    const response = await axiosInstance.get<ApiResponse<DashboardStatistics>>(`${dashboardUrl}/statistics`)
    return response.data.data // Extract data from ApiResponse wrapper
}

/**
 * Tanstack Query composable for dashboard statistics.
 *
 * Features:
 * - Automatic refetching on window focus
 * - 5 minute stale time (matches backend cache)
 * - Retry on failure
 *
 * @returns Tanstack Query result
 */
export function useDashboardStatistics() {
    return useQuery({
        queryKey: ['dashboard', 'statistics'],
        queryFn: fetchDashboardStatistics,
        staleTime: 5 * 60 * 1000, // 5 minutes (matches backend cache)
        gcTime: 10 * 60 * 1000, // 10 minutes
        refetchOnWindowFocus: true,
        retry: 2,
    })
}
