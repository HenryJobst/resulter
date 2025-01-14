export const aboutRouting = [
    {
        path: '/:locale/about',
        name: 'about-page',
        component: () => import('@/features/about/AboutPage.vue'),
    },
]
