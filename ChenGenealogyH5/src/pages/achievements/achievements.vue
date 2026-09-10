<template>
  <view class="page">
    <scroll-view scroll-x class="cats">
      <text v-for="c in cats" :key="c" class="cat" :class="{ on: cat === c }" @click="pick(c)">{{ c }}</text>
    </scroll-view>
    <view class="card" v-for="a in list" :key="a.id" @click="open(a)">
      <text class="tag">{{ a.category }}</text>
      <text class="muted" style="margin-left: 8rpx">{{ a.source }}</text>
      <view class="card-title" style="margin-top: 12rpx">{{ a.name }} · {{ a.title }}</view>
      <view class="muted">{{ (a.content || '').slice(0, 60) }}…</view>
    </view>
  </view>
</template>
<script setup lang="ts">
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { GenealogyContentApi } from '@/api/genealogy'
import { stripHtml } from '@/utils'

const cats = ['全部', '义士', '乡贤', '医者', '商贾', '文官', '武将', '学者']
const cat = ref('全部')
const list = ref<any[]>([])
const load = async () => {
  const data: any = await GenealogyContentApi.deedPage({
    pageNo: 1,
    pageSize: 50,
    category: cat.value === '全部' ? undefined : cat.value
  })
  list.value = data.list || []
}
const pick = (c: string) => {
  cat.value = c
  load()
}
const open = async (a: any) => {
  const d: any = await GenealogyContentApi.getDeed(a.id)
  uni.showModal({ title: `${d.name} · ${d.title}`, content: stripHtml(d.content) || '暂无内容', showCancel: false })
}
onLoad(load)
</script>
<style scoped>
.cats { white-space: nowrap; margin-bottom: 16rpx; }
.cat { display: inline-block; padding: 10rpx 24rpx; margin-right: 12rpx; border-radius: 999rpx; background: #fffdf7; color: #5c554a; }
.cat.on { background: #a63d2f; color: #fff; }
</style>
