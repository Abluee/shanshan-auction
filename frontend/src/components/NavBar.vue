<template>
  <div class="nav-bar">
    <div class="nav-content">
      <a-space>
        <a-button 
          v-if="showBack" 
          type="link" 
          @click="router.back()"
        >
          <template #icon>
            <left-outlined />
          </template>
          返回
        </a-button>
        <div class="title">{{ title }}</div>
      </a-space>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import { LeftOutlined } from '@ant-design/icons-vue'

defineProps<{
  title: string
  showBack?: boolean
}>()

const router = useRouter()
</script>

<style scoped>
.nav-bar {
  position: sticky;
  top: 0;
  z-index: 100;
  background: #fff;
  border-bottom: 1px solid #f0f0f0;
}

.nav-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 16px 24px;
  display: flex;
  align-items: center;
}

.title {
  font-size: 18px;
  font-weight: bold;
}
</style> 