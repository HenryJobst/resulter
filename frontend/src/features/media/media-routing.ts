export const mediaRouting = [
    {
        path: '/:locale/media',
        name: 'media-index',
        component: () => import('@/features/media/MediaIndex.vue'),
        children: [
            {
                path: '',
                name: 'media-list',
                component: () => import('@/features/media/pages/MediaList.vue'),
            },
            {
                path: 'import',
                name: 'media-new',
                component: () => import('@/features/media/pages/MediaImport.vue'),
            },
            {
                path: ':id/edit',
                name: 'media-edit',
                props: true,
                component: () => import('@/features/media/pages/MediaEdit.vue'),
            },
        ],
    },
]
