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
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { PlusOutlined } from '@ant-design/icons-vue'
import type { AuctionItem } from '../types/auction'
import { sortAuctionItems } from '../types/auction'
import CreateAuctionModal from '../components/CreateAuctionModal.vue'
import NavBar from '../components/NavBar.vue'
import { message } from 'ant-design-vue'
import AuctionCard from '../components/AuctionCard.vue'
import request from '@/utils/request'

const router = useRouter()
const showCreateModal = ref(false)
const activeTab = ref('selected')
const quickBidVisible = ref(false)
const selectedItem = ref<AuctionItem | null>(null)
const auctionItems = ref<AuctionItem[]>([])
const loading = ref(false)

const fetchAuctions = async () => {
  try {
    loading.value = true
    const response = await request.get('/items/list')
    auctionItems.value = sortAuctionItems(response || [])
  } catch (_) {
  } finally {
    loading.value = false
  }
}

const handleCreateAuction = async (auction: AuctionItem) => {
  await fetchAuctions()
  showCreateModal.value = false
}

const showQuickBid = (item: AuctionItem) => {
  selectedItem.value = item
  quickBidVisible.value = true
}

onMounted(() => {
  fetchAuctions()
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
