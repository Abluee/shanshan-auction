<template>
  <nav-bar title="每日拍卖专场" />
  <div class="auction-list">
    <div class="header">
      <div class="tabs">
        <a-tabs v-model:activeKey="activeTab">
          <a-tab-pane key="selected" tab="精选"></a-tab-pane>
<!--          <a-tab-pane key="myBids" tab="我参拍的">-->
<!--            <template #tab>-->
<!--              <span>我参拍的</span>-->
<!--              <a-badge-->
<!--                :count="myBidsCount"-->
<!--                :number-style="{ backgroundColor: '#ff4d4f' }"-->
<!--                style="margin-left: 8px"-->
<!--              />-->
<!--            </template>-->
<!--          </a-tab-pane>-->
        </a-tabs>
      </div>
    </div>

    <div class="auction-items">
      <template v-if="auctionItems.length">
        <div v-for="item in auctionItems" :key="item.id" class="auction-item" @click="goToDetail(item.id)">
          <div class="item-image">
            <img :src="item.images[0]" :alt="item.title" />
            <div class="item-overlay" :class="getAuctionStatus(item)">
              <div class="status-badge" :class="getAuctionStatus(item)">
                <template v-if="getAuctionStatus(item) === 'ended'">
                  <span>已结束</span>
                </template>
                <template v-else-if="getAuctionStatus(item) === 'notstarted'">
                  <span>未开始</span>
                </template>
                <template v-else>
                  <span>进行中</span>
                </template>
              </div>

              <div v-if="getAuctionStatus(item) === 'ended'" class="winner-info">
                <template v-if="item.bidHistory.length">
                  <div class="winner-avatar">
                    <a-avatar :src="getMockAvatar(item.bidHistory[0].userId)" />
                  </div>
                  <div class="winner-detail">
                    <div class="winner-label">获胜者</div>
                    <div class="winner-name">用户{{ item.bidHistory[0].userId.slice(-1) }}</div>
                  </div>
                </template>
                <div v-else class="no-winner">
                  <span>流拍</span>
                </div>
              </div>

              <div class="countdown" v-else>
                <countdown-timer
                  :end-time="item.endTime"
                  :start-time="item.startTime"
                  :status="item.status"
                />
              </div>
            </div>
          </div>
          <div class="item-info">
            <div class="item-title">{{ item.title }}</div>
<!--            <div class="item-tags">-->
<!--              <a-tag color="orange">假货包赔</a-tag>-->
<!--              <a-tag color="green">48小时发货</a-tag>-->
<!--            </div>-->
            <div class="item-price">
              <span class="current-price">¥{{ item.currentPrice }}</span>
<!--              <span class="original-price">¥{{ item.startPrice * 3 }}</span>-->
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
                v-if="getAuctionStatus(item) === 'ongoing'"
                @click.stop="showQuickBid(item)"
              >
                出价
              </a-button>
              <span v-else-if="getAuctionStatus(item) === 'ended'" class="ended-text">已结束</span>
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
import {ref, computed, onMounted, onUnmounted} from 'vue'
import { useRouter } from 'vue-router'
import { PlusOutlined } from '@ant-design/icons-vue'
import type { AuctionItem } from '../types/auction'
import CountdownTimer from '../components/CountdownTimer.vue'
import CreateAuctionModal from '../components/CreateAuctionModal.vue'
import NavBar from '../components/NavBar.vue'
import { useAuctionStore } from '../stores/auction'
import {message} from "ant-design-vue";

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

const handleQuickBid = async (amount: number) => {
  if (!selectedItem.value) return

  try {
    await store.placeBid(selectedItem.value.id, amount)
    quickBidVisible.value = false
  } catch (error: any) {
    message.error(error.message || '出价失败')
  }
}

const myBidsCount = computed(() =>
  store.auctionItems.filter(item =>
    item.bidHistory.some(bid => bid.userId === currentUserId)
  ).length
)

const getAuctionStatus = (item: AuctionItem) => {
  return item.status.toLowerCase()
}
const timer = null;

onMounted(async () => {
  await store.fetchAuctions()
  // 定期刷新数据
  // !timer && setInterval(() => {
  //   store.fetchAuctions()
  // }, 1000)  // 每5秒刷新一次
})

onUnmounted(()=>{
  if (timer) {
    clearInterval(timer)
  }
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

.item-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  padding: 12px;
  backdrop-filter: blur(8px);
  transition: all 0.3s ease;

  &.ended {
    background: linear-gradient(
      180deg,
      rgba(245, 34, 45, 0.2) 0%,
      rgba(245, 34, 45, 0.4) 100%
    );
    border: 1px solid rgba(245, 34, 45, 0.1);
  }

  &.notstarted {
    background: linear-gradient(
      180deg,
      rgba(24, 144, 255, 0.2) 0%,
      rgba(24, 144, 255, 0.4) 100%
    );
    border: 1px solid rgba(24, 144, 255, 0.1);
  }

  &.ongoing {
    background: linear-gradient(
      180deg,
      rgba(82, 196, 26, 0.2) 0%,
      rgba(82, 196, 26, 0.4) 100%
    );
    border: 1px solid rgba(82, 196, 26, 0.1);
  }
}

.auction-item:hover .item-overlay {
  &.ended {
    background: linear-gradient(
      180deg,
      rgba(245, 34, 45, 0.3) 0%,
      rgba(245, 34, 45, 0.5) 100%
    );
  }

  &.notstarted {
    background: linear-gradient(
      180deg,
      rgba(24, 144, 255, 0.3) 0%,
      rgba(24, 144, 255, 0.5) 100%
    );
  }

  &.ongoing {
    background: linear-gradient(
      180deg,
      rgba(82, 196, 26, 0.3) 0%,
      rgba(82, 196, 26, 0.5) 100%
    );
  }
}

.status-badge {
  position: absolute;
  top: 12px;
  right: 12px;
  padding: 6px 12px;
  border-radius: 4px;
  color: #fff;
  font-weight: bold;
  font-size: 12px;
  backdrop-filter: blur(4px);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
  transition: all 0.3s ease;

  &.ended {
    background: linear-gradient(135deg, #ff4d4f 0%, #cf1322 100%);
    border: 1px solid rgba(255, 77, 79, 0.3);
  }

  &.notstarted {
    background: linear-gradient(135deg, #1890ff 0%, #096dd9 100%);
    border: 1px solid rgba(24, 144, 255, 0.3);
  }

  &.ongoing {
    background: linear-gradient(135deg, #52c41a 0%, #389e0d 100%);
    border: 1px solid rgba(82, 196, 26, 0.3);
  }

  &:hover {
    transform: translateY(-1px);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
  }
}

.winner-info {
  background: rgba(0, 0, 0, 0.4);
  border-radius: 8px;
  padding: 12px;
  display: flex;
  align-items: center;
  gap: 12px;
  backdrop-filter: blur(4px);
}

.winner-detail {
  color: #fff;
}

.winner-label {
  font-size: 12px;
  opacity: 0.8;
}

.winner-name {
  font-weight: bold;
}

.no-winner {
  color: #fff;
  font-weight: bold;
  text-align: center;
  width: 100%;
}

.countdown {
  background: rgba(0, 0, 0, 0.4);
  border-radius: 8px;
  padding: 12px;
  color: #fff;
  text-align: center;
  backdrop-filter: blur(4px);
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

.empty-state {
  background: #fff;
  padding: 48px;
  border-radius: 8px;
  text-align: center;
}
</style>
