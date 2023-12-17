import AboutPage from '@/features/about/AboutPage.vue'

export const aboutRouting = [
  {
    path: '/:locale/about',
    name: 'about-page',
    component: AboutPage
  }
]
