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
        <auction-card
          v-for="item in auctionItems"
          :key="item.id"
          :item="item"
          @bid="showQuickBid"
        />
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
import AuctionCard from '../components/AuctionCard.vue'

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
}

.header {
  margin-bottom: 16px;
}

.auction-items {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  gap: 16px;
}

.empty-state {
  background: #fff;
  padding: 48px;
  border-radius: 8px;
  text-align: center;
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
</style>
