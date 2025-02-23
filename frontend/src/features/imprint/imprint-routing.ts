export const imprintRouting = [
    {
        path: '/:locale/imprint',
        name: 'imprint-page',
        component: () => import('@/features/imprint/ImprintPage.vue'),
    },
]
