export const personRouting = [
    {
        path: '/:locale/person',
        name: 'person-index',
        component: () => import('@/features/person/PersonIndex.vue'),
        children: [
            {
                path: '',
                name: 'person-list',
                component: () => import('@/features/person/pages/PersonList.vue'),
            },
            {
                path: 'new',
                name: 'person-new',
                component: () => import('@/features/person/pages/PersonNew.vue'),
            },
            {
                path: 'person/:id',
                name: 'person-edit',
                props: true,
                component: () => import('@/features/person/pages/PersonEdit.vue'),
            },
            {
                path: 'person/:id/merge',
                name: 'person-merge',
                props: true,
                component: () => import('@/features/person/pages/PersonMerge.vue'),
            },
        ],
    },
]
