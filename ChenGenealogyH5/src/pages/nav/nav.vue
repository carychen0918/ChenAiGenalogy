<template>
  <view class="page">
    <view class="card">
      <view class="card-title">{{ tomb?.name || '温蒂坟地' }}</view>
      <view>{{ tomb?.address || '地点信息待管理员配置' }}</view>
      <button class="btn-primary" style="margin-top: 24rpx" :disabled="!canNav" @click="go">
        {{ canNav ? '打开地图导航' : '地点信息待管理员配置' }}
      </button>
    </view>
  </view>
</template>
<script setup lang="ts">
import { computed, ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { GenealogyContentApi } from '@/api/genealogy'
import { openNav, requireLogin } from '@/utils'

const tomb = ref<any>()
const canNav = computed(() => tomb.value?.longitude && tomb.value?.latitude)
const go = () => openNav(tomb.value.longitude, tomb.value.latitude, tomb.value.name || tomb.value.address)
onLoad(async () => {
  if (!requireLogin()) return
  try { tomb.value = await GenealogyContentApi.getTomb() } catch {}
})
</script>
