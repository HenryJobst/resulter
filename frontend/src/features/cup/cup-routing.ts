import CupIndex from '@/features/cup/CupIndex.vue'
import CupList from '@/features/cup/pages/CupList.vue'
import CupNew from '@/features/cup/pages/CupNew.vue'
import CupEdit from '@/features/cup/pages/CupEdit.vue'
import CupResults from '@/features/cup/pages/CupResults.vue'

export const cupRouting = [
    {
        path: '/:locale/cup',
        name: 'cup-index',
        component: CupIndex,
        children: [
            {
                path: '',
                name: 'cup-list',
                component: CupList,
            },
            {
                path: 'new',
                name: 'cup-new',
                component: CupNew,
            },
            {
                path: 'edit/:id',
                name: 'cup-edit',
                props: true,
                component: CupEdit,
            },
            {
                path: 'results/:id',
                name: 'cup-results',
                props: true,
                component: CupResults,
            },
        ],
    },
]
