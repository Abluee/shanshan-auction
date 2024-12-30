<template>
  <nav-bar title="商品管理" />
  <div class="admin-items">
    <div class="header">
      <a-button type="primary" @click="showCreateModal = true">
        <plus-outlined />新增商品
      </a-button>
    </div>

    <a-table
      :columns="columns"
      :data-source="items"
      :loading="loading"
      row-key="id"
    >
      <template #status="{ text }">
        <a-tag :color="getStatusColor(text)">
          {{ getStatusText(text) }}
        </a-tag>
      </template>

      <template #action="{ record }">
        <a-space>
          <a-button type="link" @click="viewItem(record.id)">查看</a-button>
          <a-popconfirm
            title="确定要删除这个商品吗？"
            @confirm="deleteItem(record.id)"
            :disabled="record.status !== AuctionStatus.NOT_STARTED"
          >
            <a-button
              type="link"
              danger
              :disabled="record.status !== AuctionStatus.NOT_STARTED"
            >
              删除
            </a-button>
          </a-popconfirm>
        </a-space>
      </template>
    </a-table>

    <create-auction-modal
      ref="modalRef"
      :visible="showCreateModal"
      @update:visible="showCreateModal = $event"
      @submit="handleSubmit"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { PlusOutlined } from '@ant-design/icons-vue'
import { message } from 'ant-design-vue'
import type { AuctionItem } from '@/types/auction'
import { AuctionStatus } from '@/types/auction'
import NavBar from '@/components/NavBar.vue'
import CreateAuctionModal from '@/components/CreateAuctionModal.vue'
import request from '@/utils/request'

const router = useRouter()
const items = ref<AuctionItem[]>([])
const loading = ref(false)
const showCreateModal = ref(false)
const modalRef = ref()

const columns = [
  { title: '标题', dataIndex: 'title', key: 'title' },
  { title: '当前价格', dataIndex: 'currentPrice', key: 'currentPrice' },
  { title: '开始时间', dataIndex: 'startTime', key: 'startTime' },
  { title: '结束时间', dataIndex: 'endTime', key: 'endTime' },
  {
    title: '状态',
    dataIndex: 'status',
    key: 'status',
    slots: { customRender: 'status' }
  },
  {
    title: '操作',
    key: 'action',
    slots: { customRender: 'action' }
  }
]

const fetchItems = async () => {
  try {
    loading.value = true
    const response = await request.get('/items/list')
    items.value = response
  } catch (_) {
  } finally {
    loading.value = false
  }
}

const handleSubmit = async (formData: any) => {
  try {
    await request.post('/items/save', formData)
    message.success('创建成功')
    showCreateModal.value = false
    modalRef.value?.resetForm()
    await fetchItems()
  } catch (_) {
  }
}

const deleteItem = async (id: string) => {
  try {
    await request.post(`/items/remove?id=${id}`)
    message.success('删除成功')
    fetchItems()
  } catch (_) {
  }
}

const viewItem = (id: string) => {
  router.push(`/auction/${id}`)
}

const getStatusColor = (status: string) => {
  switch (status) {
    case AuctionStatus.ONGOING:
      return 'processing'
    case AuctionStatus.NOT_STARTED:
      return 'success'
    case AuctionStatus.ENDED:
      return 'error'
    default:
      return 'default'
  }
}

const getStatusText = (status: string) => {
  switch (status) {
    case AuctionStatus.ONGOING:
      return '进行中'
    case AuctionStatus.NOT_STARTED:
      return '未开始'
    case AuctionStatus.ENDED:
      return '已结束'
    default:
      return '未知'
  }
}

onMounted(fetchItems)
</script>

<style scoped>
.admin-items {
  padding: 24px;
}

.header {
  margin-bottom: 16px;
  display: flex;
  justify-content: flex-end;
}
</style>
