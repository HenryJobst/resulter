export const organisationRouting = [
    {
        path: '/:locale/organisation',
        name: 'organisation-index',
        component: () => import('@/features/organisation/OrganisationIndex.vue'),
        children: [
            {
                path: '',
                name: 'organisation-list',
                component: () => import('@/features/organisation/pages/OrganisationList.vue'),
            },
            {
                path: 'new',
                name: 'organisation-new',
                component: () => import('@/features/organisation/pages/OrganisationNew.vue'),
            },
            {
                path: 'organisation/:id',
                name: 'organisation-edit',
                props: true,
                component: () => import('@/features/organisation/pages/OrganisationEdit.vue'),
            },
        ],
    },
]
