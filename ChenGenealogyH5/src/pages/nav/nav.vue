<template>
  <view class="page">
    <view class="card">
      <view class="card-title">{{ tomb?.name || '温蒂坟地' }}</view>
      <view>{{ tomb?.address || '地点信息待管理员配置' }}</view>
      <amap-view v-if="canNav" class="map" :points="points" height="360px" />
      <button class="btn-primary" style="margin-top: 24rpx" :disabled="!canNav" @click="go">
        {{ canNav ? '高德导航前往' : '地点信息待管理员配置' }}
      </button>
    </view>
  </view>
</template>
<script setup lang="ts">
import { computed, ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import AmapView from '@/components/amap-view.vue'
import { GenealogyContentApi } from '@/api/genealogy'
import { openNav, requireLogin } from '@/utils'
import type { AmapPoint } from '@/utils/amap'

const tomb = ref<any>()
const canNav = computed(() => tomb.value?.longitude && tomb.value?.latitude)
const points = computed<AmapPoint[]>(() => {
  if (!canNav.value) return []
  return [{
    lng: Number(tomb.value.longitude),
    lat: Number(tomb.value.latitude),
    title: tomb.value.name || '温蒂坟地',
    content: tomb.value.address
  }]
})
const go = () => openNav(tomb.value.longitude, tomb.value.latitude, tomb.value.name || tomb.value.address)
onLoad(async () => {
  if (!requireLogin()) return
  try { tomb.value = await GenealogyContentApi.getTomb() } catch {}
})
</script>
<style scoped>
.map { margin-top: 24rpx; }
</style>
