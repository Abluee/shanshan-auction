<template>
  <a-modal
    :visible="visible"
    title="创建拍卖"
    @ok="handleOk"
    @cancel="handleCancel"
  >
    <a-form :model="formState" layout="vertical">
      <a-form-item label="商品名称" required>
        <a-input v-model:value="formState.title" />
      </a-form-item>

      <a-form-item label="起拍价" required>
        <a-input-number 
          v-model:value="formState.startPrice"
          :min="0"
          :precision="2"
          style="width: 200px"
        />
      </a-form-item>

      <a-form-item label="加价幅度" required>
        <a-input-number 
          v-model:value="formState.incrementAmount"
          :min="1"
          :precision="2"
          style="width: 200px"
        />
      </a-form-item>

      <a-form-item label="开始时间" required>
        <a-date-picker 
          v-model:value="formState.startTime"
          :show-time="true"
          format="YYYY-MM-DD HH:mm:ss"
          style="width: 200px"
        />
      </a-form-item>

      <a-form-item label="结束时间" required>
        <a-date-picker 
          v-model:value="formState.endTime"
          :show-time="true"
          format="YYYY-MM-DD HH:mm:ss"
          style="width: 200px"
        />
      </a-form-item>

      <a-form-item label="延时规则(分钟)" required>
        <a-input-number 
          v-model:value="formState.delayDuration"
          :min="1"
          :max="60"
          style="width: 200px"
        />
      </a-form-item>
    </a-form>
  </a-modal>
</template>

<script setup lang="ts">
import { reactive } from 'vue'
import type { AuctionItem } from '../types/auction'
import dayjs from 'dayjs'

defineProps<{
  visible: boolean
}>()

const emit = defineEmits<{
  'update:visible': [value: boolean]
  'create': [auction: AuctionItem]
}>()

const formState = reactive({
  title: '',
  startPrice: 0,
  incrementAmount: 1,
  startTime: null as any,
  endTime: null as any,
  delayDuration: 5,
})

const handleOk = () => {
  // 验证表单
  if (!formState.title || !formState.startTime || !formState.endTime) {
    return
  }

  // 生成随机图片
  const randomImages = Array.from({ length: 3 }, (_, index) => 
    `https://picsum.photos/800/800?random=${Date.now() + index}`
  )

  const auction: AuctionItem = {
    id: Date.now().toString(),
    title: formState.title,
    startPrice: formState.startPrice,
    currentPrice: formState.startPrice,
    incrementAmount: formState.incrementAmount,
    startTime: dayjs(formState.startTime).format('YYYY-MM-DD HH:mm:ss'),
    endTime: dayjs(formState.endTime).format('YYYY-MM-DD HH:mm:ss'),
    delayDuration: formState.delayDuration,
    status: 'notStarted',
    images: randomImages,
    bidHistory: []
  }

  emit('create', auction)
  emit('update:visible', false)
  
  // 重置表单
  Object.assign(formState, {
    title: '',
    startPrice: 0,
    incrementAmount: 1,
    startTime: null,
    endTime: null,
    delayDuration: 5,
  })
}

const handleCancel = () => {
  emit('update:visible', false)
}
</script>

<style scoped>
:deep(.ant-upload-select) {
  width: 104px;
  height: 104px;
}
</style> 