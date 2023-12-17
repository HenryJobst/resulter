import { createRouter, createWebHistory } from 'vue-router'
import { eventRouting } from '@/features/event/event-routing'
import { startRouting } from '@/features/start/start-routing'
import { aboutRouting } from '@/features/about/about-routing'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [...startRouting, ...aboutRouting, ...eventRouting]
})

export default router
