import OrganisationIndex from '@/features/organisation/OrganisationIndex.vue'
import OrganisationList from '@/features/organisation/pages/OrganisationList.vue'
import OrganisationNew from '@/features/organisation/pages/OrganisationNew.vue'
import OrganisationEdit from '@/features/organisation/pages/OrganisationEdit.vue'

export const organisationRouting = [
  {
    path: '/:locale/organisation',
    name: 'organisation-index',
    component: OrganisationIndex,
    children: [
      {
        path: '',
        name: 'organisation-list',
        component: OrganisationList
      },
      {
        path: 'new',
        name: 'organisation-new',
        component: OrganisationNew
      },
      {
        path: 'organisation/:id',
        name: 'organisation-edit',
        props: true,
        component: OrganisationEdit
      }
    ]
  }
]
