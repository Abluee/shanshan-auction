import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { useAuctionStore } from '../stores/auction'

const router = createRouter({
  history: createWebHistory(),
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
    },
    {
      path: '/admin/items',
      component: () => import('@/views/admin/ItemManagement.vue'),
      meta: { requiresAdmin: true }
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
    if (to.meta.requiresAdmin && !userStore.isAdmin) {
      next('/login')
    } else {
      next()
    }
  }
})

export default router
