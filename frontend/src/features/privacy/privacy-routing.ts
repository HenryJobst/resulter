export const privacyRouting = [
    {
        path: '/:locale/privacy',
        name: 'privacy-page',
        component: () => import('@/features/privacy/PrivacyPage.vue'),
    },
]
