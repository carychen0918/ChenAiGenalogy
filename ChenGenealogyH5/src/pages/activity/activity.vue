<template>
  <view class="page">
    <view class="card">
      <view class="card-title">{{ a.title }}</view>
      <view class="muted">{{ a.place }} · {{ formatDate(a.startTime) }}</view>
      <view>{{ a.content }}</view>
      <view v-if="a.maxPeople">名额 {{ a.registeredCount || 0 }}/{{ a.maxPeople }}</view>
      <amap-view v-if="a.longitude && a.latitude" class="map" :points="mapPoints" height="320px" />
      <button v-if="a.longitude && a.latitude" class="btn-primary" style="margin-top: 20rpx" @click="nav">高德导航前往</button>
    </view>
    <view class="card" v-if="!registered">
      <view class="card-title">报名参加</view>
      <input class="input" type="number" v-model="peopleCount" placeholder="人数" />
      <input class="input" v-model="mobile" placeholder="联系电话（选填）" />
      <button class="btn-primary" :loading="loading" @click="register">提交报名</button>
    </view>
    <view class="card" v-else>
      <view class="card-title">已报名</view>
      <view class="muted">您已报名本活动</view>
    </view>
  </view>
</template>
<script setup lang="ts">
import { computed, ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { GenealogyActivityApi } from '@/api/genealogy'
import { formatDate, openNav, requireLogin } from '@/utils'
import AmapView from '@/components/amap-view.vue'
import type { AmapPoint } from '@/utils/amap'

const a = ref<any>({})
const peopleCount = ref('1')
const mobile = ref('')
const loading = ref(false)
const registered = ref(false)
const id = ref(0)
const mapPoints = computed<AmapPoint[]>(() => {
  if (!a.value?.longitude || !a.value?.latitude) return []
  return [{
    lng: Number(a.value.longitude),
    lat: Number(a.value.latitude),
    title: a.value.place || a.value.title,
    content: a.value.gatherPlace || ''
  }]
})
const nav = () => openNav(a.value.longitude, a.value.latitude, a.value.place || a.value.title)

onLoad(async (q) => {
  if (!requireLogin()) return
  id.value = Number(q?.id)
  if (!id.value) return
  a.value = await GenealogyActivityApi.get(id.value)
  try {
    const mine: any[] = (await GenealogyActivityApi.myRegistrations()) || []
    registered.value = mine.some((r) => r.activityId === id.value)
  } catch {}
})

const register = async () => {
  loading.value = true
  try {
    await GenealogyActivityApi.register({
      activityId: id.value,
      peopleCount: Number(peopleCount.value) || 1,
      mobile: mobile.value
    })
    registered.value = true
    uni.showToast({ title: '报名成功', icon: 'success' })
  } catch (e: any) {
    uni.showToast({ title: e?.message || '报名失败', icon: 'none' })
  } finally {
    loading.value = false
  }
}
</script>
<style scoped>
.input { background: #f4ece0; border-radius: 12rpx; padding: 22rpx 24rpx; margin-bottom: 20rpx; }
.map { margin-top: 20rpx; }
</style>
