<template>
  <a-drawer
    title="出价"
    :open="visible"
    @close="handleClose"
    width="320px"
  >
    <div class="bid-form">
      <div class="current-price">
        当前价格: ¥{{ auction.currentPrice }}
      </div>
      
      <div class="increment-info">
        加价幅度: ¥{{ auction.incrementAmount }}
      </div>

      <a-form layout="vertical">
        <a-form-item label="出价金额">
          <a-input-number 
            v-model:value="bidAmount"
            :min="auction.currentPrice + auction.incrementAmount"
            :step="auction.incrementAmount"
            style="width: 200px"
          />
        </a-form-item>

        <a-form-item label="模拟用户">
          <a-select v-model:value="selectedUser" style="width: 200px">
            <a-select-option v-for="user in mockUsers" :key="user.id" :value="user.id">
              {{ user.name }}
            </a-select-option>
          </a-select>
        </a-form-item>

        <a-button type="primary" @click="handleBid" :disabled="!isValidBid">
          确认出价
        </a-button>
      </a-form>
    </div>
  </a-drawer>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import dayjs from 'dayjs'
import type { AuctionItem, Bid } from '../types/auction'

const props = defineProps<{
  visible: boolean
  auction: AuctionItem
}>()

const emit = defineEmits<{
  'update:visible': [value: boolean]
  'bid': [bid: Bid]
}>()

// 模拟用户数据
const mockUsers = [
  { id: 'user1', name: '用户1', avatar: 'https://picsum.photos/50/50' },
  { id: 'user2', name: '用户2', avatar: 'https://picsum.photos/50/51' },
  { id: 'user3', name: '用户3', avatar: 'https://picsum.photos/50/52' },
]

const bidAmount = ref(props.auction.currentPrice + props.auction.incrementAmount)
const selectedUser = ref(mockUsers[0].id)

const isValidBid = computed(() => {
  return bidAmount.value >= props.auction.currentPrice + props.auction.incrementAmount
})

const handleClose = () => {
  emit('update:visible', false)
}

const handleBid = () => {
  if (!isValidBid.value) return

  const bid: Bid = {
    id: Date.now().toString(),
    userId: selectedUser.value,
    price: bidAmount.value,
    time: dayjs().format('YYYY-MM-DD HH:mm:ss')
  }

  emit('bid', bid)
}
</script>

<style scoped>
.bid-form {
  padding: 16px;
}

.current-price {
  font-size: 20px;
  color: #f5222d;
  margin-bottom: 16px;
}

.increment-info {
  color: #666;
  margin-bottom: 24px;
}
</style> 