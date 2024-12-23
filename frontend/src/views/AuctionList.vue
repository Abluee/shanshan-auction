<template>
  <nav-bar title="每日拍卖专场" />
  <div class="auction-list">
    <div class="header">
      <div class="tabs">
        <a-tabs v-model:activeKey="activeTab">
          <a-tab-pane key="selected" tab="精选"></a-tab-pane>
          <a-tab-pane key="myBids" tab="我参拍的">
            <template #tab>
              <span>我参拍的</span>
              <a-badge 
                :count="myBidsCount" 
                :number-style="{ backgroundColor: '#ff4d4f' }"
                style="margin-left: 8px"
              />
            </template>
          </a-tab-pane>
        </a-tabs>
      </div>
    </div>

    <div class="auction-items">
      <template v-if="auctionItems.length">
        <div v-for="item in auctionItems" :key="item.id" class="auction-item" @click="goToDetail(item.id)">
          <div class="item-image">
            <img :src="item.images[0]" :alt="item.title" />
            <div class="countdown" v-if="item.status !== 'ended'">
              <countdown-timer 
                :end-time="item.endTime" 
                :start-time="item.startTime"
                :status="item.status"
              />
            </div>
            <div class="status-badge" :class="item.status">
              <template v-if="item.status === 'ended'">
                <div class="winner">
                  <span>已结束</span>
                  <div v-if="item.bidHistory.length" class="winner-info">
                    <a-avatar :src="getMockAvatar(item.bidHistory[0].userId)" size="small" />
                    <span>获胜者: 用户{{ item.bidHistory[0].userId.slice(-1) }}</span>
                  </div>
                </div>
              </template>
              <template v-else-if="item.status === 'notStarted'">
                <span>未开始</span>
              </template>
            </div>
          </div>
          <div class="item-info">
            <div class="item-title">{{ item.title }}</div>
            <div class="item-tags">
              <a-tag color="orange">假货包赔</a-tag>
              <a-tag color="green">48小时发货</a-tag>
            </div>
            <div class="item-price">
              <span class="current-price">¥{{ item.currentPrice }}</span>
              <span class="original-price">¥{{ item.startPrice * 3 }}</span>
            </div>
            <div class="item-footer">
              <div class="bid-info">
                <span>{{ item.bidHistory.length }}次出价</span>
                <span class="bidders">
                  <a-avatar-group :max-count="3" size="small">
                    <a-avatar v-for="bid in item.bidHistory.slice(0,3)" :key="bid.id" :src="getMockAvatar(bid.userId)" />
                  </a-avatar-group>
                </span>
              </div>
              <a-button 
                type="primary" 
                shape="round"
                v-if="item.status === 'ongoing'"
                @click.stop="showQuickBid(item)"
              >
                出价
              </a-button>
              <span v-else-if="item.status === 'ended'" class="ended-text">已结束</span>
              <span v-else class="not-started-text">未开始</span>
            </div>
          </div>
        </div>
      </template>
      <template v-else>
        <div class="empty-state">
          <a-empty 
            :description="activeTab === 'myBids' ? '您还没有参与任何拍卖' : '暂无拍卖商品'"
          >
            <template #extra>
              <a-button type="primary" @click="activeTab = 'selected'" v-if="activeTab === 'myBids'">
                去看看
              </a-button>
            </template>
          </a-empty>
        </div>
      </template>
    </div>

    <a-float-button 
      type="primary" 
      @click="showCreateModal = true"
      style="position: fixed; right: 24px; bottom: 24px;"
    >
      <template #icon>
        <plus-outlined />
      </template>
    </a-float-button>

    <create-auction-modal 
      :visible="showCreateModal"
      @update:visible="showCreateModal = $event"
      @create="handleCreateAuction"
    />

    <!-- 快速出价抽屉 -->
    <a-drawer
      :open="quickBidVisible"
      placement="bottom"
      height="300"
      @close="quickBidVisible = false"
    >
      <template #title>
        <div class="quick-bid-title">
          <span>快速出价</span>
          <span class="current-price">当前价: ¥{{ selectedItem?.currentPrice }}</span>
        </div>
      </template>
      <div class="quick-bid-content">
        <div class="bid-buttons">
          <a-button 
            v-for="amount in quickBidAmounts" 
            :key="amount"
            type="primary"
            ghost
            size="large"
            @click="handleQuickBid(amount)"
          >
            ¥{{ amount }}
          </a-button>
        </div>
      </div>
    </a-drawer>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { PlusOutlined } from '@ant-design/icons-vue'
import type { AuctionItem } from '../types/auction'
import CountdownTimer from '../components/CountdownTimer.vue'
import CreateAuctionModal from '../components/CreateAuctionModal.vue'
import NavBar from '../components/NavBar.vue'
import { useAuctionStore } from '../stores/auction'
import dayjs from 'dayjs'

const router = useRouter()
const store = useAuctionStore()
const showCreateModal = ref(false)
const activeTab = ref('selected')
const quickBidVisible = ref(false)
const selectedItem = ref<AuctionItem | null>(null)
const currentUserId = 'user1'

const auctionItems = computed(() => {
  if (activeTab.value === 'myBids') {
    return store.auctionItems.filter(item => 
      item.bidHistory.some(bid => bid.userId === currentUserId)
    )
  }
  return store.auctionItems
})

const quickBidAmounts = computed(() => {
  if (!selectedItem.value) return []
  const current = selectedItem.value.currentPrice
  const increment = selectedItem.value.incrementAmount
  return [
    current + increment,
    current + increment * 2,
    current + increment * 5,
    current + increment * 10
  ]
})

const getMockAvatar = (userId: string) => {
  return `https://picsum.photos/32/32?random=${userId}`
}

const goToDetail = (id: string) => {
  router.push(`/auction/${id}`)
}

const handleCreateAuction = (auction: AuctionItem) => {
  store.addAuction(auction)
  showCreateModal.value = false
}

const showQuickBid = (item: AuctionItem) => {
  selectedItem.value = item
  quickBidVisible.value = true
}

const handleQuickBid = (amount: number) => {
  if (!selectedItem.value) return
  
  const bid = {
    id: Date.now().toString(),
    userId: 'user1',
    price: amount,
    time: dayjs().format('YYYY-MM-DD HH:mm:ss')
  }
  
  store.placeBid(selectedItem.value.id, bid)
  
  quickBidVisible.value = false
}

const myBidsCount = computed(() => 
  store.auctionItems.filter(item => 
    item.bidHistory.some(bid => bid.userId === currentUserId)
  ).length
)

onMounted(() => {
  // 定期更新所有商品状态
  setInterval(() => {
    store.updateAllAuctionStatus()
  }, 1000)
})
</script>

<style scoped>
.auction-list {
  padding: 16px;
  background: #f5f5f5;
}

.header {
  background: #fff;
  padding: 16px;
  margin-bottom: 16px;
  border-radius: 8px;
}

.title {
  display: flex;
  align-items: baseline;
  gap: 8px;
  margin-bottom: 16px;
}

.subtitle {
  color: #666;
  font-size: 14px;
}

.auction-items {
  display: grid;
  gap: 16px;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
}

.auction-item {
  background: #fff;
  border-radius: 8px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s;
}

.auction-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 2px 12px rgba(0,0,0,0.1);
}

.item-image {
  position: relative;
  padding-top: 100%;
}

.item-image img {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.countdown {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  background: rgba(0,0,0,0.6);
  color: #fff;
  padding: 8px;
  text-align: center;
}

.item-info {
  padding: 12px;
}

.item-title {
  font-size: 14px;
  margin-bottom: 8px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.item-tags {
  margin-bottom: 8px;
}

.item-price {
  margin-bottom: 12px;
}

.current-price {
  color: #ff4d4f;
  font-size: 20px;
  font-weight: bold;
  margin-right: 8px;
}

.original-price {
  color: #999;
  text-decoration: line-through;
  font-size: 14px;
}

.item-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.bid-info {
  color: #666;
  font-size: 12px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.quick-bid-title {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.bid-buttons {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
  padding: 16px;
}

.status-badge {
  position: absolute;
  top: 0;
  right: 0;
  padding: 8px 16px;
  color: #fff;
  font-weight: bold;
  
  &.ended {
    background: rgba(245, 34, 45, 0.8);
  }
  
  &.notStarted {
    background: rgba(24, 144, 255, 0.8);
  }
}

.winner {
  text-align: center;
  
  .winner-info {
    display: flex;
    align-items: center;
    gap: 8px;
    margin-top: 8px;
    font-size: 12px;
  }
}

.empty-state {
  background: #fff;
  padding: 48px;
  border-radius: 8px;
  text-align: center;
}
</style> 