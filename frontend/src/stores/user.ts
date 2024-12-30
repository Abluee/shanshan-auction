import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import request from '@/utils/request'

export const useUserStore = defineStore('user', () => {
  const userId = ref<number | null>(null)
  const username = ref<string>('')
  const nickname = ref<string>('')
  const avatar = ref<string>('')
  const token = ref<string>('')
  const role = ref<string>('')

  const isLoggedIn = computed(() => !!token.value)
  const isAdmin = computed(() => role.value === 'ADMIN')

  async function login(username: string, password: string) {
    try {
      const response = await request.post('/auth/login', { username, password })
      setUser(response)
      return response
    } catch (error) {
      console.error('Login failed:', error)
      throw error
    }
  }

  async function logout() {
    try {
      await request.post('/auth/logout')
    } catch (error) {
      console.error('Logout failed:', error)
    } finally {
      clearUser()
    }
  }

  function setUser(user: any) {
    userId.value = user.userId
    username.value = user.username
    nickname.value = user.nickname
    avatar.value = user.avatar
    token.value = user.token
    role.value = user.role
    // 保存到本地存储
    localStorage.setItem('user', JSON.stringify(user))
  }

  function clearUser() {
    userId.value = null
    username.value = ''
    nickname.value = ''
    avatar.value = ''
    token.value = ''
    role.value = ''
    // 清除本地存储
    localStorage.removeItem('user')
  }

  // 初始化时从本地存储加载用户信息
  const storedUser = localStorage.getItem('user')
  if (storedUser) {
    setUser(JSON.parse(storedUser))
  }

  return {
    userId,
    username,
    nickname,
    avatar,
    token,
    role,
    isLoggedIn,
    isAdmin,
    login,
    logout,
    setUser,
    clearUser
  }
}) 