<template>
  <view class="page">
    <view class="card" v-for="n in nodes" :key="n.id" @click="open(n)">
      <view class="muted">{{ n.nodeTime }}</view>
      <view class="card-title">{{ n.eventTitle }}</view>
      <view>{{ n.place }} {{ n.person ? '· ' + n.person : '' }}</view>
      <view class="muted">{{ n.description }}</view>
      <text v-if="n.longitude && n.latitude" class="link">查看地图位置</text>
      <text v-else class="tag">信息待补充</text>
    </view>
    <view class="card" v-if="family.originContent">
      <view class="card-title">姓氏源流</view>
      <rich-text :nodes="family.originContent" />
    </view>
    <view v-if="!nodes.length" class="muted empty">暂无迁徙节点</view>
  </view>
</template>
<script setup lang="ts">
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { GenealogyContentApi } from '@/api/genealogy'
import { openNav } from '@/utils'

const nodes = ref<any[]>([])
const family = ref<any>({})
const open = (n: any) => {
  if (n.longitude && n.latitude) openNav(n.longitude, n.latitude, n.place)
}
onLoad(async () => {
  try { family.value = await GenealogyContentApi.getFamily() } catch {}
  try { nodes.value = (await GenealogyContentApi.migrationList()) || [] } catch {}
})
</script>
<style scoped>
.link { color: #a63d2f; font-size: 24rpx; margin-top: 8rpx; display: inline-block; }
.empty { text-align: center; padding: 80rpx 0; }
</style>
