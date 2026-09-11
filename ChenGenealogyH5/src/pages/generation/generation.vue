<template>
  <view class="page">
    <view class="card">
      <view class="card-title">派语对照表</view>
      <scroll-view scroll-x class="table-scroll">
        <view class="table">
          <view class="tr head">
            <text v-for="col in POEM_COLS" :key="col.key" class="th">{{ col.label }}</text>
          </view>
          <view class="tr" v-for="row in rows" :key="row.generationNo">
            <text v-for="col in POEM_COLS" :key="col.key" class="td">{{ cell(row[col.key]) }}</text>
          </view>
        </view>
      </scroll-view>
      <view v-if="!rows.length" class="muted empty">字辈录入中</view>
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

const POEM_COLS = [
  { key: 'chituOrder', label: '赤土官庄世序' },
  { key: 'nationalSource', label: '全国统一字派' },
  { key: 'jiaofangSource', label: '高安椒坊字派' },
  { key: 'house1', label: '长房' },
  { key: 'house2', label: '二房' },
  { key: 'house3', label: '三房' },
  { key: 'house3Zhijin', label: '三房织金' },
  { key: 'house45', label: '四五房' }
]
const rows = ref<any[]>([])
const fatherGen = ref<number>()
const word = ref('')
const cell = (v?: string) => (v && String(v).trim() ? String(v).trim() : '—')
const genLabels = computed(() => rows.value.map((g) => g.chituOrder || g.generationNo + '世'))
const fatherLabel = computed(() => {
  const row = rows.value.find((g) => g.generationNo === fatherGen.value)
  return row ? row.chituOrder || row.generationNo + '世' : ''
})
const nameChar = (raw: string) => raw.replace(/（[^）]*）/g, '').replace(/\([^)]*\)/g, '')
const examples = computed(() => {
  if (!word.value || word.value === '未定') return ''
  return word.value.split('、').map((w) => {
    const ch = nameChar(w)
    return `陈${ch}华、陈${ch}宇`
  }).join('；')
})
const onPick = async (e: any) => {
  const g = rows.value[Number(e.detail.value)]
  if (!g) return
  fatherGen.value = g.generationNo
  word.value = await GenealogyContentApi.recommend(g.generationNo)
}
onLoad(async () => {
  rows.value = (await GenealogyContentApi.poemTable()) || []
})
</script>
<style scoped>
.table-scroll { width: 100%; margin-top: 12rpx; }
.table { min-width: 1280rpx; border: 1px solid #e8dfcc; border-radius: 8rpx; overflow: hidden; }
.tr { display: flex; }
.tr.head { background: #f4ece0; }
.th, .td {
  flex: 1;
  min-width: 150rpx;
  padding: 16rpx 8rpx;
  text-align: center;
  font-size: 22rpx;
  border-right: 1px solid #eadfcb;
  border-bottom: 1px solid #eadfcb;
  box-sizing: border-box;
}
.th { font-weight: 700; color: #5c5144; }
.td { color: #2a251f; }
.tr:last-child .td { border-bottom: 0; }
.th:last-child, .td:last-child { border-right: 0; }
.picker { background: #f4ece0; padding: 20rpx; border-radius: 12rpx; }
.result { margin-top: 16rpx; font-weight: 700; color: #a63d2f; }
.empty { text-align: center; padding: 24rpx 0; }
</style>
