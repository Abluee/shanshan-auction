import { createRouter, createWebHashHistory } from 'vue-router'

const router = createRouter({
  history: createWebHashHistory('/'),
  routes: [
    {
      path: '/',
      component: () => import('../views/AuctionList.vue')
    },
    {
      path: '/auction/:id',
      component: () => import('../views/AuctionDetail.vue')
    }
  ]
})

export default router 