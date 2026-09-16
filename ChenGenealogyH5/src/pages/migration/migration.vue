<template>
  <view class="page">
    <view class="card">
      <view class="card-title">迁徙地图</view>
      <amap-view ref="mapRef" :points="mapPoints" show-line height="360px" @select="onMapSelect" />
    </view>
    <view
      class="card story"
      :class="{ active: activeId === n.id }"
      v-for="n in nodes"
      :key="n.id"
      @click="focusNode(n)"
    >
      <view class="muted">{{ n.nodeTime }}</view>
      <view class="card-title">{{ n.eventTitle }}</view>
      <view>{{ n.place }} {{ n.person ? ' · ' + n.person : '' }}</view>
      <view class="muted">{{ n.description }}</view>
      <text v-if="n.longitude && n.latitude" class="link" @click.stop="open(n)">高德导航 / 查看位置</text>
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
import { computed, ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import AmapView from '@/components/amap-view.vue'
import { GenealogyContentApi } from '@/api/genealogy'
import { openNav } from '@/utils'
import type { AmapPoint } from '@/utils/amap'

const nodes = ref<any[]>([])
const family = ref<any>({})
const mapRef = ref<any>()
const activeId = ref<number | string>()
const mapPoints = computed<AmapPoint[]>(() =>
  nodes.value
    .filter((n) => n.longitude && n.latitude)
    .map((n) => ({
      id: n.id,
      lng: Number(n.longitude),
      lat: Number(n.latitude),
      title: n.eventTitle || n.place,
      content: [n.nodeTime, n.place, n.person, n.description].filter(Boolean).join('\n')
    }))
)
const focusNode = (n: any) => {
  activeId.value = n.id
  if (n.longitude && n.latitude) mapRef.value?.focus?.(n.id)
}
const onMapSelect = (p: AmapPoint) => {
  activeId.value = p.id
}
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
.story.active { border-color: #a63d2f; box-shadow: 0 0 0 2rpx #a63d2f; }
</style>
