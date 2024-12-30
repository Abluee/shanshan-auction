<template>
  <div class="auction-item" @click="goToDetail(item.id)">
    <div class="item-image">
      <img :src="item.images[0]" :alt="item.title" />
      <div class="status-badge" :class="item.status.toLowerCase()">
        <template v-if="item.status === AuctionStatus.ENDED">
          <span>已结束</span>
        </template>
        <template v-else-if="item.status === AuctionStatus.NOT_STARTED">
          <span>未开始</span>
        </template>
        <template v-else>
          <span>进行中</span>
        </template>
      </div>
      <div class="item-overlay" :class="getAuctionStatus(item)">

        <div v-if="item.status === AuctionStatus.ENDED" class="winner-info">
          <template v-if="bidHistory.length">
            <div class="winner-avatar">
              <a-avatar :src="getMockAvatar(bidHistory[0].userId)" />
            </div>
            <div class="winner-detail">
              <div class="winner-label">获胜者</div>
              <div class="winner-name">{{ bidHistory[0].username }}</div>
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
      <div class="item-price">
        <span class="current-price">¥{{ item.currentPrice }}</span>
      </div>
      <div class="item-footer">
        <div class="bid-info">
          <span>{{ bidHistory.length }}次出价</span>
          <span class="bidders">
            <a-avatar-group :max-count="3" size="small">
              <a-avatar
                v-for="bid in bidHistory.slice(0,3)"
                :key="bid.id"
                :src="bid.userAvatar || getMockAvatar(bid.userId)"
              />
            </a-avatar-group>
          </span>
        </div>
        <a-button
          type="primary"
          shape="round"
          v-if="item.status === AuctionStatus.ONGOING"
        >
          出价
        </a-button>
        <span v-else-if="item.status === AuctionStatus.ENDED" class="ended-text">已结束</span>
        <span v-else class="not-started-text">未开始</span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import type { AuctionItem, Bid } from '../types/auction'
import { AuctionStatus } from '../types/auction'
import CountdownTimer from './CountdownTimer.vue'
import request from '@/utils/request'

const props = defineProps<{
  item: AuctionItem
}>()

defineEmits<{
  (e: 'bid', item: AuctionItem): void
}>()

const router = useRouter()
const bidHistory = ref<Bid[]>([])
const loading = ref(false)

const getMockAvatar = (userId: string) => {
  return `https://picsum.photos/32/32?random=${userId}`
}

const getAuctionStatus = (item: AuctionItem) => {
  return item.status.toLowerCase()
}

const goToDetail = (id: string) => {
  router.push(`/auction/${id}`)
}

const fetchBidHistory = async () => {
  try {
    loading.value = true
    const response = await request.get(`/bids/history/${props.item.id}`)
    bidHistory.value = response || []
  } catch (error) {
    console.error('Failed to fetch bid history:', error)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchBidHistory()
})
</script>

<style scoped lang="less">
.auction-item {
  background: #fff;
  border-radius: 8px;
  overflow: hidden;
  transition: all 0.3s ease;
  cursor: pointer;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.auction-item:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 24px rgba(0, 0, 0, 0.12);
}

.item-image {
  position: relative;
  width: 100%;
  padding-bottom: 100%;
  overflow: hidden;
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
  left: 0;
  right: 0;
  bottom: 0;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  padding: 12px;
  transition: all 0.3s ease;

  &.ended {
    border: 1px solid rgba(245, 34, 45, 0.1);
  }

  &.not_started {
    border: 1px solid rgba(24, 144, 255, 0.1);
  }

  &.ongoing {
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

  &.not_started {
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
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
  transition: all 0.3s ease;

  &.ended {
    background: #ff4d4f;
    border: 1px solid rgba(255, 77, 79, 0.3);
  }

  &.not_started {
    background: #1890ff;
    border: 1px solid rgba(24, 144, 255, 0.3);
  }

  &.ongoing {
    background: #52c41a;
    border: 1px solid rgba(82, 196, 26, 0.3);
  }

  &:hover {
    transform: translateY(-1px);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
  }
}

.winner-info {
  background: rgba(0, 0, 0, 0.6);
  border-radius: 8px;
  padding: 12px;
  display: flex;
  align-items: center;
  gap: 12px;
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
  background: rgba(0, 0, 0, 0.6);
  border-radius: 8px;
  padding: 12px;
  color: #fff;
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

.item-price {
  margin-bottom: 12px;
}

.current-price {
  color: #ff4d4f;
  font-size: 20px;
  font-weight: bold;
  margin-right: 8px;
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

.ended-text, .not-started-text {
  color: #999;
  font-size: 14px;
}
</style>
