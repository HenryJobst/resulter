import StartPage from '@/features/start/StartPage.vue'

export const startRouting = [
  {
    path: '/:locale/',
    name: 'start-page',
    component: StartPage
  }
]
