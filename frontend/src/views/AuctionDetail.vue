<template>
  <nav-bar :title="currentItem?.title || '商品详情'" :show-back="true" />
  <div class="auction-detail" v-if="currentItem">
    <div class="image-gallery">
      <a-carousel>
        <div v-for="image in currentItem.images" :key="image">
          <img :src="image" :alt="currentItem.title" />
        </div>
      </a-carousel>
    </div>

    <div class="info-section">
      <h1 class="title">{{ currentItem.title }}</h1>
      <p class="description">{{ currentItem.description }}</p>

      <div class="price-info">
        <div class="current-price">
          <span class="label">当前价格</span>
          <span class="value">¥{{ currentItem.currentPrice }}</span>
        </div>
        <div class="start-price">
          <span class="label">起拍价</span>
          <span class="value">¥{{ currentItem.startPrice }}</span>
        </div>
        <div class="increment">
          <span class="label">加价幅度</span>
          <span class="value">¥{{ currentItem.incrementAmount }}</span>
        </div>
      </div>

      <div class="status-info">
        <countdown-timer
          :end-time="currentItem.endTime"
          :start-time="currentItem.startTime"
          :status="currentItem.status"
        />
      </div>

      <div class="bid-section" v-if="currentItem.status === AuctionStatus.ONGOING">
        <div class="quick-bid-buttons">
          <a-button
            v-for="amount in quickBidAmounts"
            :key="amount"
            type="primary"
            ghost
            @click="handleBid(amount)"
          >
            ¥{{ amount }}
          </a-button>
        </div>
        <a-input-number
          v-model:value="customBidAmount"
          :min="currentItem.currentPrice + currentItem.incrementAmount"
          :step="currentItem.incrementAmount"
          style="width: 200px"
        />
        <a-button type="primary" @click="handleBid(customBidAmount)">
          确认出价
        </a-button>
      </div>
    </div>

    <div class="bid-history">
      <h2>出价记录</h2>
      <a-list :data-source="bidHistory">
        <template #renderItem="{ item: bid }">
          <a-list-item>
            <a-list-item-meta>
              <template #avatar>
                <a-avatar :src="bid.userAvatar || getMockAvatar(bid.userId)" />
              </template>
              <template #title>{{ bid.username }}</template>
              <template #description>
                出价 ¥{{ bid.price }} 于 {{ formatTime(bid.createdAt) }}
              </template>
            </a-list-item-meta>
          </a-list-item>
        </template>
      </a-list>
    </div>
  </div>
  <div v-else-if="loading" class="loading">
    <a-spin />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRoute } from 'vue-router'
import { message, Modal} from 'ant-design-vue'
import dayjs from 'dayjs'
import type { AuctionItem, Bid } from '../types/auction'
import { AuctionStatus } from '../types/auction'
import CountdownTimer from '../components/CountdownTimer.vue'
import NavBar from '../components/NavBar.vue'
import request from '@/utils/request'

const route = useRoute()
const currentItem = ref<AuctionItem | null>(null)
const bidHistory = ref<Bid[]>([])
const customBidAmount = ref<number>()
const refreshTimer = ref<number | null>(null)
const loading = ref(false)

const fetchItemDetail = async (id: string) => {
  try {
    loading.value = true
    const response = await request.get(`/items/${id}`)
    currentItem.value = response
  } catch (error) {
    console.error('Failed to fetch item:', error)
    message.error('获取商品详情失败')
  } finally {
    loading.value = false
  }
}

const fetchBidHistory = async (id: string) => {
  try {
    const response = await request.get(`/bids/history/${id}`)
    bidHistory.value = response || []
  } catch (error) {
    console.error('Failed to fetch bid history:', error)
  }
}

const handleBid = async (amount: number) => {
  try {
    const confirmed = await new Promise((resolve) => {
      Modal.confirm({
        title: '确认出价',
        content: `您确定要出价 ${amount} 元吗？`,
        okText: '确认',
        cancelText: '取消',
        onOk: () => resolve(true),
        onCancel: () => resolve(false)
      })
    })

    if (!confirmed) return
    if (!currentItem.value || !amount) return

    await request.post('/bids/place', {
      itemId: currentItem.value.id,
      price: amount
    })
    message.success('出价成功')
    // 刷新数据
    await fetchItemDetail(currentItem.value.id)
    await fetchBidHistory(currentItem.value.id)
  } catch (error: any) {
    message.error(error.message || '出价失败')
  }
}

const quickBidAmounts = computed(() => {
  if (!currentItem.value) return []
  const current = currentItem.value.currentPrice
  const increment = currentItem.value.incrementAmount
  return [
    current + increment,
    current + increment * 2,
    current + increment * 3,
    current + increment * 4
  ]
})

const formatTime = (time: string) => {
  return dayjs(time).format('YYYY-MM-DD HH:mm:ss')
}

const getMockAvatar = (userId: string) => {
  return `https://picsum.photos/32/32?random=${userId}`
}

onMounted(async () => {
  const id = route.params.id as string
  await fetchItemDetail(id)
  await fetchBidHistory(id)

  // 设置定时刷新
  refreshTimer.value = window.setInterval(async () => {
    await fetchItemDetail(id)
    await fetchBidHistory(id)
  }, 3000)
})

onUnmounted(() => {
  if (refreshTimer.value) {
    clearInterval(refreshTimer.value)
    refreshTimer.value = null
  }
})
</script>

<style scoped>
.auction-detail {
  padding: 16px;
  max-width: 800px;
  margin: 0 auto;
}

.image-gallery {
  margin-bottom: 24px;
  border-radius: 8px;
  overflow: hidden;
}

.image-gallery img {
  width: 100%;
  height: 400px;
  object-fit: cover;
}

.info-section {
  background: #fff;
  padding: 24px;
  border-radius: 8px;
  margin-bottom: 24px;
}

.title {
  font-size: 24px;
  margin-bottom: 16px;
}

.description {
  color: #666;
  margin-bottom: 24px;
}

.price-info {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
  margin-bottom: 24px;
}

.price-info .label {
  color: #666;
  font-size: 14px;
  display: block;
}

.price-info .value {
  color: #ff4d4f;
  font-size: 24px;
  font-weight: bold;
}

.status-info {
  margin-bottom: 24px;
  text-align: center;
}

.bid-section {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.quick-bid-buttons {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 8px;
}

.bid-history {
  background: #fff;
  padding: 24px;
  border-radius: 8px;
}

.loading {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 400px;
}
</style>
