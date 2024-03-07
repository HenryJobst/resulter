import MediaIndex from '@/features/media/MediaIndex.vue'
import MediaList from '@/features/media/pages/MediaList.vue'
import MediaImport from '@/features/media/pages/MediaImport.vue'
import MediaEdit from '@/features/media/pages/MediaEdit.vue'

export const mediaRouting = [
  {
    path: '/:locale/media',
    name: 'media-index',
    component: MediaIndex,
    children: [
      {
        path: '',
        name: 'media-list',
        component: MediaList
      },
      {
        path: 'import',
        name: 'media-new',
        component: MediaImport
      },
      {
        path: ':id/edit',
        name: 'media-edit',
        props: true,
        component: MediaEdit
      }
    ]
  }
]
