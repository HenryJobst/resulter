import type { RouteRecordRaw } from 'vue-router'
import { useAuthStore } from '@/features/auth/store/auth.store'

export const analysisRoutes: RouteRecordRaw[] = [
    {
        path: '/:locale/analysis',
        name: 'analysis-hub',
        component: () => import('@/features/analysis/pages/AnalysisHub.vue'),
    },
    {
        path: '/:locale/analysis/mental-resilience',
        name: 'mental-resilience-analysis',
        component: () => import('@/features/analysis/pages/MentalResilienceAnalysis.vue'),
        props: route => ({
            scope: route.query.scope || 'event',
            resultListId: route.query.resultListId ? Number(route.query.resultListId) : undefined,
            cupId: route.query.cupId ? Number(route.query.cupId) : undefined,
            years: route.query.years
                ? String(route.query.years).split(',').map(Number)
                : undefined,
            eventName: route.query.eventName ? String(route.query.eventName) : undefined,
            resultListLabel: route.query.resultListLabel ? String(route.query.resultListLabel) : undefined,
        }),
    },
    {
        path: '/:locale/analysis/cheat-detection',
        name: 'cheat-detection-analysis',
        component: () => import('@/features/analysis/pages/AnomalyDetectionAnalysis.vue'),
        beforeEnter: (to, from, next) => {
            const authStore = useAuthStore()
            if (authStore.isAdmin) {
                next()
            }
            else {
                // Redirect to analysis hub if not admin
                next({ name: 'analysis-hub' })
            }
        },
        props: route => ({
            scope: route.query.scope || 'event',
            resultListId: route.query.resultListId ? Number(route.query.resultListId) : undefined,
            cupId: route.query.cupId ? Number(route.query.cupId) : undefined,
            years: route.query.years
                ? String(route.query.years).split(',').map(Number)
                : undefined,
            eventName: route.query.eventName ? String(route.query.eventName) : undefined,
            resultListLabel: route.query.resultListLabel ? String(route.query.resultListLabel) : undefined,
        }),
    },
    {
        path: '/:locale/analysis/hanging-detection',
        name: 'hanging-detection-analysis',
        component: () => import('@/features/analysis/pages/HangingDetectionAnalysis.vue'),
        beforeEnter: (to, from, next) => {
            const authStore = useAuthStore()
            if (authStore.isAdmin) {
                next()
            }
            else {
                // Redirect to analysis hub if not admin
                next({ name: 'analysis-hub' })
            }
        },
        props: route => ({
            scope: route.query.scope || 'event',
            resultListId: route.query.resultListId ? Number(route.query.resultListId) : undefined,
            cupId: route.query.cupId ? Number(route.query.cupId) : undefined,
            years: route.query.years
                ? String(route.query.years).split(',').map(Number)
                : undefined,
            eventName: route.query.eventName ? String(route.query.eventName) : undefined,
            resultListLabel: route.query.resultListLabel ? String(route.query.resultListLabel) : undefined,
        }),
    },
    {
        path: '/:locale/analysis/split-time-table',
        name: 'split-time-table-analysis',
        component: () => import('@/features/analysis/pages/SplitTimeTableAnalysis.vue'),
        props: route => ({
            resultListId: route.query.resultListId ? Number(route.query.resultListId) : undefined,
            eventName: route.query.eventName ? String(route.query.eventName) : undefined,
            resultListLabel: route.query.resultListLabel ? String(route.query.resultListLabel) : undefined,
        }),
    },
]
