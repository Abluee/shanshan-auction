<template>
  <div class="nav-bar">
    <div class="left">
      <a-button v-if="showBack" type="link" @click="router.back()">
        <left-outlined />
      </a-button>
      <span class="title">{{ title }}</span>
    </div>
    <div class="right">
      <template v-if="userStore.isLoggedIn">
        <a-dropdown>
          <a class="user-info" @click.prevent>
            <a-avatar :src="userStore.avatar || defaultAvatar" />
            <span class="nickname">{{ userStore.nickname }}</span>
          </a>
          <template #overlay>
            <a-menu>
              <a-menu-item v-if="userStore.isAdmin" key="admin">
                <router-link to="/admin">管理后台</router-link>
              </a-menu-item>
              <a-menu-item key="profile">
                <router-link to="/profile">个人中心</router-link>
              </a-menu-item>
              <a-menu-item key="logout" @click="handleLogout">
                退出登录
              </a-menu-item>
            </a-menu>
          </template>
        </a-dropdown>
      </template>
      <template v-else>
        <router-link to="/login">
          <a-button type="primary">登录</a-button>
        </router-link>
      </template>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import { LeftOutlined } from '@ant-design/icons-vue'
import { useUserStore } from '../stores/user'
import { message } from 'ant-design-vue'

const props = defineProps<{
  title: string
  showBack?: boolean
}>()

const router = useRouter()
const userStore = useUserStore()
const defaultAvatar = 'https://picsum.photos/32/32'

const handleLogout = async () => {
  try {
    await userStore.logout()
    message.success('退出成功')
    router.push('/login')
  } catch (error) {
    message.error('退出失败')
  }
}
</script>

<style scoped>
.nav-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 16px;
  height: 64px;
  background: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.left {
  display: flex;
  align-items: center;
  gap: 8px;
}

.title {
  font-size: 18px;
  font-weight: 500;
}

.right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
}

.nickname {
  color: rgba(0, 0, 0, 0.85);
}
</style> 