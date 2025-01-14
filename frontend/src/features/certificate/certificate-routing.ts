export const certificateRouting = [
    {
        path: '/:locale/certificate',
        name: 'certificate-index',
        component: () => import('@/features/certificate/CertificateIndex.vue'),
        children: [
            {
                path: '',
                name: 'certificate-list',
                component: () => import('@/features/certificate/pages/CertificateList.vue'),
            },
            {
                path: 'new',
                name: 'certificate-new',
                props: true,
                component: () => import('@/features/certificate/pages/CertificateNew.vue'),
            },
            {
                path: ':id/edit',
                name: 'certificate-edit',
                props: true,
                component: () => import('@/features/certificate/pages/CertificateEdit.vue'),
            },
        ],
    },
]
