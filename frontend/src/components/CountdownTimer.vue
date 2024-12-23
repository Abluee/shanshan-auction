<template>
  <div>
    <span v-if="status === 'notStarted'">
      距开始: {{ timeLeft }}
    </span>
    <span v-else-if="status === 'ongoing'">
      距结束: {{ timeLeft }}
    </span>
    <span v-else>
      已结束
    </span>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import dayjs from 'dayjs'

const props = defineProps<{
  startTime: string,
  endTime: string,
  status: 'notStarted' | 'ongoing' | 'ended'
}>()

const timeLeft = ref('')
let timer: ReturnType<typeof setInterval>

const updateTime = () => {
  const now = dayjs()
  const target = props.status === 'notStarted' 
    ? dayjs(props.startTime)
    : dayjs(props.endTime)
  
  const diff = target.diff(now, 'second')
  
  if (diff <= 0) {
    timeLeft.value = '00:00:00'
    return
  }
  
  const hours = Math.floor(diff / 3600)
  const minutes = Math.floor((diff % 3600) / 60)
  const seconds = diff % 60
  
  timeLeft.value = `${hours.toString().padStart(2, '0')}:${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}`
}

onMounted(() => {
  updateTime()
  timer = setInterval(updateTime, 1000)
})

onUnmounted(() => {
  clearInterval(timer)
})
</script> 