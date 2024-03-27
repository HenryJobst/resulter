import CertificateIndex from '@/features/certificate/CertificateIndex.vue'
import CertificateList from '@/features/certificate/pages/CertificateList.vue'
import CertificateNew from '@/features/certificate/pages/CertificateNew.vue'
import CertificateEdit from '@/features/certificate/pages/CertificateEdit.vue'

export const certificateRouting = [
    {
        path: '/:locale/certificate',
        name: 'certificate-index',
        component: CertificateIndex,
        children: [
            {
                path: '',
                name: 'certificate-list',
                component: CertificateList,
            },
            {
                path: 'new',
                name: 'certificate-new',
                props: true,
                component: CertificateNew,
            },
            {
                path: ':id/edit',
                name: 'certificate-edit',
                props: true,
                component: CertificateEdit,
            },
        ],
    },
]
