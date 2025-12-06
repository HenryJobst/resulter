import type { RouteRecordRaw } from 'vue-router'

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
]
