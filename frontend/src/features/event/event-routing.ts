import EventIndex from '@/features/event/EventIndex.vue'
import EventList from '@/features/event/pages/EventList.vue'
import EventNew from '@/features/event/pages/EventNew.vue'
import EventEdit from '@/features/event/pages/EventEdit.vue'
import EventImport from '@/features/event/pages/EventImport.vue'
import EventResults from '@/features/event/pages/EventResults.vue'

export const eventRouting = [
  {
    path: '/:locale/event',
    name: 'event-index',
    component: EventIndex,
    children: [
      {
        path: '',
        name: 'event-list',
        component: EventList
      },
      {
        path: 'new',
        name: 'event-new',
        component: EventNew
      },
      {
        path: 'import',
        name: 'event-import',
        component: EventImport
      },
      {
        path: 'edit/:id',
        name: 'event-edit',
        props: true,
        component: EventEdit
      },
      {
        path: 'results/:id',
        name: 'event-results',
        props: true,
        component: EventResults
      }
    ]
  }
]
