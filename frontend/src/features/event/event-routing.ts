export const eventRouting = [
    {
        path: '/:locale/event',
        name: 'event-index',
        component: () => import('@/features/event/EventIndex.vue'),
        children: [
            {
                path: '',
                name: 'event-list',
                component: () => import('@/features/event/pages/EventList.vue'),
            },
            {
                path: 'new',
                name: 'event-new',
                component: () => import('@/features/event/pages/EventNew.vue'),
            },
            {
                path: 'import',
                name: 'event-import',
                component: () => import('@/features/event/pages/EventImport.vue'),
            },
            {
                path: ':id/edit',
                name: 'event-edit',
                props: true,
                component: () => import('@/features/event/pages/EventEdit.vue'),
            },
            {
                path: ':id/results',
                name: 'event-results',
                props: true,
                component: () => import('@/features/event/pages/EventResults.vue'),
            },
            {
                path: ':id/results/:resultListId/split-time-analysis',
                name: 'split-time-analysis',
                props: true,
                component: () => import('@/features/event/pages/SplitTimeAnalysis.vue'),
            },
        ],
    },
]
