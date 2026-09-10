<template>
  <view class="page">
    <view class="card" v-for="g in groups" :key="g.generationNo">
      <view class="card-title">{{ g.generationNo }}世</view>
      <view class="words">
        <text class="tag" v-for="item in g.items" :key="item.id">{{ item.word }} · {{ status(item.status) }}</text>
      </view>
      <view class="muted">{{ g.items.map((i: any) => i.remark).filter(Boolean).join('；') }}</view>
    </view>
    <view class="card">
      <view class="card-title">新生儿取名推荐</view>
      <picker :range="genLabels" @change="onPick">
        <view class="picker">{{ fatherLabel || '选择父亲世代' }}</view>
      </picker>
      <view v-if="word" class="result">推荐下一辈字辈：{{ word }}</view>
      <view v-if="examples" class="muted">{{ examples }}</view>
    </view>
  </view>
</template>
<script setup lang="ts">
import { computed, ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { GenealogyContentApi } from '@/api/genealogy'
import { GEN_STATUS } from '@/utils'

const list = ref<any[]>([])
const fatherGen = ref<number>()
const word = ref('')
const groups = computed(() => {
  const map = new Map<number, any[]>()
  for (const g of list.value) {
    if (!map.has(g.generationNo)) map.set(g.generationNo, [])
    map.get(g.generationNo)!.push(g)
  }
  return [...map.entries()].sort((a, b) => a[0] - b[0]).map(([generationNo, items]) => ({ generationNo, items }))
})
const genLabels = computed(() => groups.value.map((g) => g.generationNo + '世'))
const fatherLabel = computed(() => (fatherGen.value ? fatherGen.value + '世' : ''))
const examples = computed(() => {
  if (!word.value || word.value === '未定') return ''
  return word.value.split('、').map((w) => `陈${w}华、陈${w}宇`).join('；')
})
const status = (s: number) => GEN_STATUS[s] || ''
const onPick = async (e: any) => {
  const g = groups.value[Number(e.detail.value)]
  fatherGen.value = g.generationNo
  word.value = await GenealogyContentApi.recommend(g.generationNo)
}
onLoad(async () => {
  list.value = (await GenealogyContentApi.generationList()) || []
})
</script>
<style scoped>
.words { display: flex; flex-wrap: wrap; gap: 12rpx; margin: 12rpx 0; }
.picker { background: #f4ece0; padding: 20rpx; border-radius: 12rpx; }
.result { margin-top: 16rpx; font-weight: 700; color: #a63d2f; }
</style>
