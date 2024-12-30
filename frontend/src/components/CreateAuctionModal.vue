<template>
  <a-modal
    :visible="visible"
    title="创建拍卖"
    @ok="handleOk"
    @cancel="handleCancel"
    :confirmLoading="loading"
    width="800px"
  >
    <a-form
      :model="formState"
      :rules="rules"
      ref="formRef"
      :label-col="{ span: 4 }"
      :wrapper-col="{ span: 20 }"
    >
      <a-form-item
        label="商品名称"
        name="title"
        :rules="[{ required: true, message: '请输入商品名称' }]"
      >
        <a-input v-model:value="formState.title" />
      </a-form-item>

      <a-form-item
        label="商品描述"
        name="description"
      >
        <a-textarea v-model:value="formState.description" :rows="4" />
      </a-form-item>

      <a-form-item
        label="起拍价"
        name="startPrice"
        :rules="[{ required: true, message: '请输入起拍价' }]"
      >
        <a-input-number
          v-model:value="formState.startPrice"
          :min="0.01"
          :precision="2"
          style="width: 200px"
        />
      </a-form-item>

      <a-form-item
        label="加价幅度"
        name="incrementAmount"
        :rules="[{ required: true, message: '请输入加价幅度' }]"
      >
        <a-input-number
          v-model:value="formState.incrementAmount"
          :min="0.01"
          :precision="2"
          style="width: 200px"
        />
      </a-form-item>

      <a-form-item
        label="开始时间"
        name="startTime"
        :rules="[{ required: true, message: '请选择开始时间' }]"
      >
        <a-date-picker
          v-model:value="formState.startTime"
          :show-time="true"
          format="YYYY-MM-DD HH:mm:ss"
          style="width: 200px"
          :disabledDate="disabledStartDate"
        />
      </a-form-item>

      <a-form-item
        label="结束时间"
        name="endTime"
        :rules="[{ required: true, message: '请选择结束时间' }]"
      >
        <a-date-picker
          v-model:value="formState.endTime"
          :show-time="true"
          format="YYYY-MM-DD HH:mm:ss"
          style="width: 200px"
          :disabledDate="disabledEndDate"
        />
      </a-form-item>

      <a-form-item
        label="延时规则"
        name="delayDuration"
        :rules="[{ required: true, message: '请输入延时规则' }]"
      >
        <a-input-number
          v-model:value="formState.delayDuration"
          :min="30"
          :max="3600"
          style="width: 200px"
          addon-after="秒"
        />
      </a-form-item>

      <a-form-item
        label="商品图片"
        name="imageUrls"
        :rules="[
          { required: true, message: '请至少添加一张图片' },
          { type: 'array', max: 9, message: '最多添加9张图片' }
        ]"
      >
        <image-url-input v-model="formState.imageUrls" />
      </a-form-item>
    </a-form>
  </a-modal>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import type { FormInstance } from 'ant-design-vue'
import ImageUrlInput from './ImageUrlInput.vue'
import type { AuctionItem } from '../types/auction'
import dayjs from 'dayjs'

const props = withDefaults(defineProps<{
  visible: boolean
}>(), {
  visible: false
})

const emit = defineEmits<{
  'update:visible': [value: boolean]
  'submit': [formData: any]
}>()

const formRef = ref<FormInstance>()
const loading = ref(false)

const formState = reactive({
  title: '',
  description: '',
  startPrice: undefined as number | undefined,
  incrementAmount: undefined as number | undefined,
  startTime: null as any,
  endTime: null as any,
  delayDuration: 30,
  imageUrls: ['']
})

const disabledStartDate = (current: Date) => {
  return current && current < dayjs().startOf('day').toDate()
}

const disabledEndDate = (current: Date) => {
  return current && (
    current < dayjs().startOf('day').toDate() ||
    (formState.startTime && current < formState.startTime)
  )
}

const rules = {
  title: [{ required: true, message: '请输入商品名称' }],
  startPrice: [{ required: true, message: '请输入起拍价' }],
  incrementAmount: [{ required: true, message: '请输入加价幅度' }],
  startTime: [{ required: true, message: '请选择开始时间' }],
  endTime: [{ required: true, message: '请选择结束时间' }],
  delayDuration: [{ required: true, message: '请输入延时规则' }],
  imageUrls: [
    { required: true, message: '请至少添加一张图片' },
    { type: 'array', max: 9, message: '最多添加9张图片' }
  ]
}

const handleOk = async () => {
  try {
    loading.value = true
    await formRef.value?.validate()

    const formData = {
      title: formState.title,
      description: formState.description || '',
      startPrice: formState.startPrice,
      incrementAmount: formState.incrementAmount,
      startTime: dayjs(formState.startTime).format('YYYY-MM-DD HH:mm:ss'),
      endTime: dayjs(formState.endTime).format('YYYY-MM-DD HH:mm:ss'),
      delayDuration: formState.delayDuration,
      imageUrls: formState.imageUrls
    }

    emit('submit', formData)
  } catch (error) {
    console.error('Validation failed:', error)
  } finally {
    loading.value = false
  }
}

const resetForm = () => {
  formRef.value?.resetFields()
  formState.imageUrls = ['']
}

const handleCancel = () => {
  emit('update:visible', false)
  resetForm()
}

defineExpose({
  resetForm
})
</script>

<style scoped>
:deep(.ant-form-item) {
  margin-bottom: 24px;
}
</style>
