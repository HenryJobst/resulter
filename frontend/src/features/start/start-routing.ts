export const startRouting = [
    {
        path: '/:locale/',
        name: 'start-page',
        component: () => import('@/features/start/StartPage.vue'),
    },
]
