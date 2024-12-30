<template>
  <nav-bar :title="item?.title || '商品详情'" :show-back="true" />
  <div class="auction-detail" v-if="item">
    <div class="image-gallery">
      <a-carousel>
        <div v-for="image in item.images" :key="image">
          <img :src="image" :alt="item.title" />
        </div>
      </a-carousel>
    </div>

    <div class="info-section">
      <h1 class="title">{{ item.title }}</h1>
      <p class="description">{{ item.description }}</p>

      <div class="price-info">
        <div class="current-price">
          <span class="label">当前价格</span>
          <span class="value">¥{{ item.currentPrice }}</span>
        </div>
        <div class="start-price">
          <span class="label">起拍价</span>
          <span class="value">¥{{ item.startPrice }}</span>
        </div>
        <div class="increment">
          <span class="label">加价幅度</span>
          <span class="value">¥{{ item.incrementAmount }}</span>
        </div>
      </div>

      <div class="status-info">
        <countdown-timer
          :end-time="item.endTime"
          :start-time="item.startTime"
          :status="item.status"
        />
      </div>

      <div class="bid-section" v-if="item.status === 'ongoing'">
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
          :min="item.currentPrice + item.incrementAmount"
          :step="item.incrementAmount"
          style="width: 200px"
        />
        <a-button type="primary" @click="handleBid(customBidAmount)">
          确认出价
        </a-button>
      </div>
    </div>

    <div class="bid-history">
      <h2>出价记录</h2>
      <a-list :data-source="item.bidHistory">
        <template #renderItem="{ item: bid }">
          <a-list-item>
            <a-list-item-meta>
              <template #avatar>
                <a-avatar :src="bid.userAvatar" />
              </template>
              <template #title>{{ bid.username }}</template>
              <template #description>
                出价 ¥{{ bid.price }} 于 {{ formatTime(bid.time) }}
              </template>
            </a-list-item-meta>
          </a-list-item>
        </template>
      </a-list>
    </div>
  </div>
  <div v-else class="loading">
    <a-spin />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { message } from 'ant-design-vue'
import dayjs from 'dayjs'
import { useAuctionStore } from '../stores/auction'
import CountdownTimer from '../components/CountdownTimer.vue'
import NavBar from '../components/NavBar.vue'

const route = useRoute()
const store = useAuctionStore()
const customBidAmount = ref<number>()
const item = computed(() => store.currentItem)

const quickBidAmounts = computed(() => {
  if (!item.value) return []
  const current = item.value.currentPrice
  const increment = item.value.incrementAmount
  return [
    current + increment,
    current + increment * 2,
    current + increment * 5,
    current + increment * 10
  ]
})

const handleBid = async (amount: number) => {
  if (!item.value || !amount) return
  try {
    await store.placeBid(item.value.id, amount)
    message.success('出价成功')
  } catch (error: any) {
    message.error(error.message || '出价失败')
  }
}

const formatTime = (time: string) => {
  return dayjs(time).format('YYYY-MM-DD HH:mm:ss')
}

onMounted(async () => {
  const id = route.params.id as string
  try {
    await store.fetchItemById(id)
    // 定期刷新数据
    setInterval(() => {
      store.fetchItemById(id)
    }, 5000)
  } catch (error: any) {
    message.error('获取商品详情失败')
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