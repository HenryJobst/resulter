export const cupRouting = [
    {
        path: '/:locale/cup',
        name: 'cup-index',
        component: () => import('@/features/cup/CupIndex.vue'),
        children: [
            {
                path: '',
                name: 'cup-list',
                component: () => import('@/features/cup/pages/CupList.vue'),
            },
            {
                path: 'new',
                name: 'cup-new',
                component: () => import('@/features/cup/pages/CupNew.vue'),
            },
            {
                path: 'edit/:id',
                name: 'cup-edit',
                props: true,
                component: () => import('@/features/cup/pages/CupEdit.vue'),
            },
            {
                path: 'results/:id',
                name: 'cup-results',
                props: true,
                component: () => import('@/features/cup/pages/CupResults.vue'),
            },
        ],
    },
]
