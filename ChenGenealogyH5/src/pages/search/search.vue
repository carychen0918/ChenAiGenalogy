<template>
  <view class="page">
    <view class="card">
      <input v-model="kw" placeholder="搜姓名 / 几世 / 字辈" confirm-type="search" @confirm="search" />
    </view>
    <view class="card member" v-for="m in list" :key="m.id" @click="open(m)">
      {{ m.name }} · {{ m.generationNo ? m.generationNo + '世' : '' }}{{ m.generationWord ? m.generationWord + '字辈' : '' }}
    </view>
    <view v-if="kw && !list.length" class="muted empty">未找到成员</view>
  </view>
</template>
<script setup lang="ts">
import { ref } from 'vue'
import { GenealogyShowcaseApi } from '@/api/genealogy'

const kw = ref('')
const list = ref<any[]>([])
const search = async () => {
  const q = kw.value.trim()
  if (!q) {
    list.value = []
    return
  }
  try { list.value = (await GenealogyShowcaseApi.search(q)) || [] } catch { list.value = [] }
}
const open = (m: any) => uni.navigateTo({ url: '/pages/member/member?id=' + m.id })
</script>
<style scoped>
input { background: #f4ece0; border-radius: 12rpx; padding: 18rpx 20rpx; }
.member { margin-bottom: 16rpx; }
.empty { text-align: center; padding: 80rpx 0; }
</style>
