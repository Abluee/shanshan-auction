<template>
  <div class="image-url-inputs">
    <div v-for="(url, index) in modelValue" :key="index" class="image-url-item">
      <a-input
        v-model:value="modelValue[index]"
        placeholder="请输入图片URL (以http://或https://开头)"
        :status="getValidationStatus(url)"
        @change="handleChange"
      >
        <template #suffix>
          <minus-outlined
            v-if="modelValue.length > 1"
            class="remove-icon"
            @click="removeUrl(index)"
          />
        </template>
      </a-input>
      <div class="preview" v-if="isValidUrl(url)">
        <img :src="url" alt="预览" />
      </div>
      <div class="error" v-if="!isValidUrl(url) && url">
        图片URL必须以http://或https://开头
      </div>
    </div>
    
    <a-button 
      v-if="modelValue.length < 9"
      type="dashed" 
      block 
      @click="addUrl"
    >
      <plus-outlined />添加图片
    </a-button>
  </div>
</template>

<script setup lang="ts">
import { PlusOutlined, MinusOutlined } from '@ant-design/icons-vue'
import { computed } from 'vue'

const props = defineProps<{
  modelValue: string[]
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', value: string[]): void
}>()

const isValidUrl = (url: string) => {
  return /^(http|https):\/\/.*$/.test(url)
}

const getValidationStatus = (url: string) => {
  if (!url) return ''
  return isValidUrl(url) ? '' : 'error'
}

const addUrl = () => {
  if (props.modelValue.length < 9) {
    emit('update:modelValue', [...props.modelValue, ''])
  }
}

const removeUrl = (index: number) => {
  const newUrls = [...props.modelValue]
  newUrls.splice(index, 1)
  emit('update:modelValue', newUrls)
}

const handleChange = () => {
  emit('update:modelValue', [...props.modelValue])
}
</script>

<style scoped>
.image-url-inputs {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.image-url-item {
  position: relative;
}

.remove-icon {
  color: #ff4d4f;
  cursor: pointer;
}

.preview {
  margin-top: 8px;
  width: 100px;
  height: 100px;
  border-radius: 4px;
  overflow: hidden;
}

.preview img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.error {
  color: #ff4d4f;
  font-size: 12px;
  margin-top: 4px;
}
</style> 