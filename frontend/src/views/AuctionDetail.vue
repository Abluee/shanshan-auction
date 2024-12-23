<template>
  <nav-bar title="商品详情" :show-back="true" />
  <div class="auction-detail">
    <div class="main-content">
      <!-- 商品图片轮播 -->
      <div class="image-section">
        <a-carousel class="carousel" autoplay>
          <div v-for="(image, index) in auction?.images || []" :key="index">
            <img :src="image" :alt="`商品图片${index + 1}`" />
          </div>
        </a-carousel>
      </div>

      <!-- 商品信息 -->
      <div class="info-section">
        <h1 class="title">{{ auction?.title }}</h1>
        
        <div class="price-section">
          <div class="current-price">
            <span class="label">当前价</span>
            <span class="amount">¥{{ auction?.currentPrice }}</span>
            <span class="original">原价: ¥{{ (auction?.startPrice || 0) * 3 }}</span>
          </div>
          
          <div class="bid-controls">
            <template v-if="auction.status === 'ongoing'">
              <div class="increment-control">
                <span class="label">加价金额</span>
                <div class="bid-input-group">
                  <a-button 
                    class="control-btn"
                    @click="decrementBid"
                    :disabled="bidAmount <= (auction?.currentPrice || 0) + (auction?.incrementAmount || 0)"
                  >
                    <minus-outlined />
                  </a-button>
                  <a-input-number
                    v-model:value="bidAmount"
                    :min="(auction?.currentPrice || 0) + (auction?.incrementAmount || 0)"
                    :step="auction?.incrementAmount"
                    size="large"
                    style="width: 120px"
                    :formatter="(value: number) => `¥${value}`"
                    :parser="(value: string) => value.replace('¥', '')"
                  />
                  <a-button 
                    class="control-btn"
                    @click="incrementBid"
                  >
                    <plus-outlined />
                  </a-button>
                </div>
                <a-button 
                  type="primary" 
                  size="large"
                  :disabled="!isValidBid"
                  @click="handleCurrentUserBid"
                  style="margin-left: 16px"
                >
                  出价
                </a-button>
              </div>
              <div class="quick-bids">
                <a-button 
                  v-for="amount in quickBidAmounts" 
                  :key="amount"
                  type="primary"
                  ghost
                  @click="handleQuickBid(amount)"
                >
                  ¥{{ amount }}
                </a-button>
              </div>
            </template>
            <template v-else-if="auction.status === 'ended'">
              <div class="end-notice">
                <div class="notice-text">拍卖已结束</div>
                <div v-if="auction.bidHistory.length" class="winner-info">
                  <a-avatar :src="getMockAvatar(auction.bidHistory[0].userId)" />
                  <div class="winner-detail">
                    <div class="winner-label">获胜者</div>
                    <div class="winner-name">用户{{ auction.bidHistory[0].userId.slice(-1) }}</div>
                    <div class="final-price">成交价: ¥{{ auction.currentPrice }}</div>
                  </div>
                </div>
              </div>
            </template>
            <template v-else>
              <div class="not-started-notice">
                拍卖尚未开始
              </div>
            </template>
          </div>

          <div class="countdown-section">
            <template v-if="auction?.status === 'ended'">
              <div class="end-status">已结束</div>
            </template>
            <template v-else>
              <countdown-timer 
                :end-time="auction?.endTime || ''" 
                :start-time="auction?.startTime || ''"
                :status="auction?.status || 'notStarted'"
              />
            </template>
          </div>
        </div>

        <div class="tags-section">
          <a-tag color="orange">假货包赔</a-tag>
          <a-tag color="green">48小时发货</a-tag>
          <a-tag color="blue">7天无理由退换</a-tag>
        </div>
      </div>
    </div>

    <!-- 出价记录 -->
    <div class="bid-history-section">
      <a-tabs>
        <a-tab-pane key="1" tab="出价记录">
          <div class="bid-list">
            <div class="bid-summary">
              <div class="total-bids">
                共 {{ auction?.bidHistory.length || 0 }} 次出价
              </div>
              <div v-if="auction?.status === 'ended'" class="end-notice">
                已结束
              </div>
            </div>
            <div class="bid-ranking">
              <div class="rank-header">
                <span>排名</span>
                <span>出价</span>
                <span>用户</span>
                <span>时间</span>
              </div>
              <template v-if="sortedBidHistory.length">
                <div v-for="(bid, index) in sortedBidHistory" 
                  :key="bid.id" 
                  class="rank-item"
                  :class="{'top-rank': index < 3}"
                >
                  <span class="rank-number">
                    <template v-if="index < 3">
                      <span class="rank-icon rank-{{ index + 1 }}">{{ index + 1 }}</span>
                    </template>
                    <template v-else>
                      {{ index + 1 }}
                    </template>
                  </span>
                  <span class="bid-price">¥{{ bid.price }}</span>
                  <span class="user-info">
                    <a-avatar :src="getMockAvatar(bid.userId)" size="small" />
                    <span>用户{{ bid.userId.slice(-1) }}</span>
                  </span>
                  <span class="bid-time">{{ formatTime(bid.time) }}</span>
                </div>
              </template>
              <div v-else class="empty-bids">
                暂无出价记录
              </div>
            </div>
          </div>
        </a-tab-pane>
        <a-tab-pane key="2" tab="商品详情">
          <div class="detail-content">
            <h3>商品描述</h3>
            <p>这是一个示例商品描述...</p>
          </div>
        </a-tab-pane>
      </a-tabs>
    </div>

    <!-- 模拟其他用户出价的悬浮按钮 -->
    <a-float-button 
      type="primary"
      style="right: 24px; bottom: 24px;"
      @click="showMockBidDrawer = true"
      v-if="auction.status === 'ongoing'"
    >
      <template #icon>
        <user-outlined />
      </template>
      <template #description>
        模拟出价
      </template>
    </a-float-button>

    <!-- 模拟其他用户出价的抽屉 -->
    <a-drawer
      title="模拟其他用户出价"
      :open="showMockBidDrawer"
      @close="showMockBidDrawer = false"
      width="320"
    >
      <a-form layout="vertical">
        <a-form-item label="选择用户">
          <a-select v-model:value="mockUserId">
            <a-select-option v-for="user in mockUsers" :key="user.id" :value="user.id">
              {{ user.name }}
            </a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="出价金额">
          <div class="mock-bid-input-group">
            <a-button 
              class="control-btn"
              @click="decrementMockBid"
              :disabled="mockBidAmount <= auction?.currentPrice + auction?.incrementAmount"
            >
              <minus-outlined />
            </a-button>
            <a-input-number 
              v-model:value="mockBidAmount"
              :min="(auction?.currentPrice || 0) + (auction?.incrementAmount || 0)"
              :step="auction?.incrementAmount"
              style="width: 120px"
              :formatter="(value: number) => `¥${value}`"
              :parser="(value: string) => value.replace('¥', '')"
            />
            <a-button 
              class="control-btn"
              @click="incrementMockBid"
            >
              <plus-outlined />
            </a-button>
          </div>
        </a-form-item>
        <a-button 
          type="primary" 
          @click="handleMockBid"
          :disabled="mockBidAmount <= auction?.currentPrice"
        >
          模拟出价
        </a-button>
      </a-form>
    </a-drawer>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { UserOutlined, MinusOutlined, PlusOutlined } from '@ant-design/icons-vue'
import dayjs from 'dayjs'
import type { Bid } from '../types/auction'
import CountdownTimer from '../components/CountdownTimer.vue'
import NavBar from '../components/NavBar.vue'
import { useAuctionStore } from '../stores/auction'

const route = useRoute()
const router = useRouter()
const store = useAuctionStore()
const showBidDrawer = ref(false)
const showMockBidDrawer = ref(false)
const mockUserId = ref('user2')
const mockBidAmount = ref(0)
const bidAmount = ref(0)
const currentUserId = 'user1'

// 模拟用户数据
const mockUsers = [
  { id: 'user2', name: '用户2', avatar: 'https://picsum.photos/50/51' },
  { id: 'user3', name: '用户3', avatar: 'https://picsum.photos/50/52' },
  { id: 'user4', name: '用户4', avatar: 'https://picsum.photos/50/53' },
]

// 修改 auction 的类型定义
const auction = computed(() => {
  const item = store.getAuctionById(route.params.id as string)
  return item || {
    id: '',
    title: '',
    images: [],
    startPrice: 0,
    currentPrice: 0,
    startTime: '',
    endTime: '',
    incrementAmount: 0,
    delayDuration: 0,
    status: 'notStarted' as const,
    bidHistory: []
  }
})

const quickBidAmounts = computed(() => {
  const current = auction.value?.currentPrice || 0
  const increment = auction.value?.incrementAmount || 0
  return [
    current + increment,
    current + increment * 2,
    current + increment * 5,
    current + increment * 10
  ]
})

const sortedBidHistory = computed(() => {
  return [...(auction.value?.bidHistory || [])].sort((a, b) => b.price - a.price)
})

const getMockAvatar = (userId: string) => {
  return `https://picsum.photos/32/32?random=${userId}`
}

const formatTime = (time: string) => {
  return dayjs(time).format('MM-DD HH:mm:ss')
}

const handleBid = (bid: Bid) => {
  if (!auction.value?.id) return
  store.placeBid(auction.value.id, bid)
  showBidDrawer.value = false
  
  // 更新出价金额
  const current = auction.value?.currentPrice || 0
  const increment = auction.value?.incrementAmount || 0
  bidAmount.value = current + increment
  mockBidAmount.value = current + increment
}

const handleQuickBid = (amount: number) => {
  const bid: Bid = {
    id: Date.now().toString(),
    userId: 'user1',
    price: amount,
    time: dayjs().format('YYYY-MM-DD HH:mm:ss')
  }
  handleBid(bid)
}

const handleMockBid = () => {
  if (mockBidAmount.value <= auction.value.currentPrice) return
  
  const bid: Bid = {
    id: Date.now().toString(),
    userId: mockUserId.value,
    price: mockBidAmount.value,
    time: dayjs().format('YYYY-MM-DD HH:mm:ss')
  }
  
  handleBid(bid)
  showMockBidDrawer.value = false
}

const isValidBid = computed(() => {
  const current = auction.value?.currentPrice || 0
  const increment = auction.value?.incrementAmount || 0
  return bidAmount.value >= current + increment
})

const incrementBid = () => {
  bidAmount.value += auction.value?.incrementAmount || 0
}

const decrementBid = () => {
  const minBid = (auction.value?.currentPrice || 0) + (auction.value?.incrementAmount || 0)
  if (bidAmount.value > minBid) {
    bidAmount.value -= auction.value?.incrementAmount || 0
  }
}

const handleCurrentUserBid = () => {
  if (!isValidBid.value) return
  
  const bid: Bid = {
    id: Date.now().toString(),
    userId: currentUserId,
    price: bidAmount.value,
    time: dayjs().format('YYYY-MM-DD HH:mm:ss')
  }
  
  handleBid(bid)
}

const incrementMockBid = () => {
  mockBidAmount.value += auction.value.incrementAmount
}

const decrementMockBid = () => {
  const minBid = auction.value.currentPrice + auction.value.incrementAmount
  if (mockBidAmount.value > minBid) {
    mockBidAmount.value -= auction.value.incrementAmount
  }
}

onMounted(() => {
  const item = store.getAuctionById(route.params.id as string)
  if (!item) {
    router.push('/')
    return
  }
  
  // 初始化出价金额
  bidAmount.value = item.currentPrice + item.incrementAmount
  mockBidAmount.value = item.currentPrice + item.incrementAmount
})
</script>

<style scoped>
.auction-detail {
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px;
}

.main-content {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 24px;
  margin-bottom: 24px;
}

.image-section {
  background: #fff;
  border-radius: 8px;
  overflow: hidden;
}

.carousel {
  height: 500px;
}

.carousel img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.info-section {
  background: #fff;
  border-radius: 8px;
  padding: 24px;
}

.title {
  font-size: 24px;
  margin-bottom: 24px;
}

.price-section {
  margin-bottom: 24px;
}

.current-price {
  margin-bottom: 16px;
}

.current-price .label {
  font-size: 16px;
  color: #666;
  margin-right: 8px;
}

.current-price .amount {
  font-size: 32px;
  color: #ff4d4f;
  font-weight: bold;
  margin-right: 16px;
}

.current-price .original {
  color: #999;
  text-decoration: line-through;
}

.bid-controls {
  margin: 24px 0;
}

.increment-control {
  margin-bottom: 16px;
  display: flex;
  align-items: center;
}

.increment-control .label {
  margin-right: 16px;
  color: #666;
}

.quick-bids {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 8px;
  margin-top: 16px;
}

.countdown-section {
  text-align: center;
  margin: 24px 0;
  font-size: 20px;
}

.tags-section {
  margin-top: 24px;
}

.bid-history-section {
  background: #fff;
  border-radius: 8px;
  padding: 24px;
}

.bid-summary {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding: 0 12px;
}

.total-bids {
  color: #666;
  font-size: 14px;
}

.end-notice {
  color: #ff4d4f;
  font-weight: bold;
}

.empty-bids {
  text-align: center;
  color: #999;
  padding: 32px 0;
}

.bid-ranking {
  .rank-header {
    display: grid;
    grid-template-columns: 80px 1fr 200px 200px;
    padding: 12px;
    background: #f5f5f5;
    font-weight: bold;
  }

  .rank-item {
    display: grid;
    grid-template-columns: 80px 1fr 200px 200px;
    padding: 12px;
    align-items: center;
    border-bottom: 1px solid #f0f0f0;

    &.top-rank {
      background: #fff7e6;
    }
  }

  .rank-number {
    display: flex;
    align-items: center;
    font-weight: bold;
    color: #666;
    
    .rank-icon {
      width: 24px;
      height: 24px;
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      color: #fff;
      font-size: 14px;
      box-shadow: 0 2px 4px rgba(0,0,0,0.2);
      
      &.rank-1 {
        background: linear-gradient(135deg, #f5222d, #ff4d4f);
      }
      
      &.rank-2 {
        background: linear-gradient(135deg, #fa8c16, #ffa940);
      }
      
      &.rank-3 {
        background: linear-gradient(135deg, #faad14, #ffc53d);
      }
    }
  }

  .bid-price {
    color: #ff4d4f;
    font-weight: bold;
  }

  .user-info {
    display: flex;
    align-items: center;
    gap: 8px;
  }

  .bid-time {
    color: #999;
  }
}

@media (max-width: 768px) {
  .main-content {
    grid-template-columns: 1fr;
  }
  
  .carousel {
    height: 300px;
  }
}

.bid-input-group {
  display: flex;
  align-items: center;
  gap: 4px;
}

.control-btn {
  height: 40px;
  width: 40px;
  padding: 0;
  display: flex;
  align-items: center;
  justify-content: center;
}

.mock-bid-input-group {
  display: flex;
  align-items: center;
  gap: 4px;
  margin-bottom: 16px;
}

.countdown-section {
  text-align: center;
  margin: 24px 0;
  font-size: 20px;
}

.end-status {
  color: #ff4d4f;
  font-weight: bold;
}
</style> 