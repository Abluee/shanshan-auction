import { createRouter, createWebHashHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { useAuctionStore } from '../stores/auction'

const router = createRouter({
  history: createWebHashHistory('./'),
  routes: [
    {
      path: '/',
      component: () => import('../views/AuctionList.vue')
    },
    {
      path: '/auction/:id',
      component: () => import('../views/AuctionDetail.vue')
    },
    {
      path: '/login',
      component: () => import('../views/Login.vue')
    }
  ]
})

router.beforeEach((to, from, next) => {
  const userStore = useUserStore()

  if (!userStore.isLoggedIn && to.path !== '/login') {
    next('/login')
  } else {
    if (from.name === 'auction-detail' && to.name !== 'auction-detail') {
      const store = useAuctionStore()
      store.clearCurrentItem()
      store.clearRefreshTimer()
    }
    next()
  }
})

export default router
