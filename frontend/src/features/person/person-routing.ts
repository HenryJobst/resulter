import PersonIndex from '@/features/person/PersonIndex.vue'
import PersonList from '@/features/person/pages/PersonList.vue'
import PersonNew from '@/features/person/pages/PersonNew.vue'
import PersonEdit from '@/features/person/pages/PersonEdit.vue'
import PersonMerge from '@/features/person/pages/PersonMerge.vue'

export const personRouting = [
    {
        path: '/:locale/person',
        name: 'person-index',
        component: PersonIndex,
        children: [
            {
                path: '',
                name: 'person-list',
                component: PersonList,
            },
            {
                path: 'new',
                name: 'person-new',
                component: PersonNew,
            },
            {
                path: 'person/:id',
                name: 'person-edit',
                props: true,
                component: PersonEdit,
            },
            {
                path: 'person/:id/merge',
                name: 'person-merge',
                props: true,
                component: PersonMerge,
            },
        ],
    },
]
