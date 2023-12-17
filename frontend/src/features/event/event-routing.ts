import EventIndex from '@/features/event/EventIndex.vue'
import EventList from '@/features/event/pages/EventList.vue'
import EventNew from '@/features/event/pages/EventNew.vue'
import EventEdit from '@/features/event/pages/EventEdit.vue'

export const eventRouting = [
  {
    path: '/events',
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
        path: 'edit/:id',
        name: 'event-edit',
        props: true,
        component: EventEdit
      }
    ]
  }
]
