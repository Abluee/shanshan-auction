import { createRouter, createWebHashHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

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

router.beforeEach((to, _from, next) => {
  const userStore = useUserStore()

  if (!userStore.isLoggedIn && to.path !== '/login') {
    next('/login')
  } else {
    next()
  }
})

export default router
