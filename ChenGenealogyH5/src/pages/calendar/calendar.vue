<template>
  <view class="page">
    <view class="card" v-for="c in list" :key="c.type + c.title + c.date" @click="go(c)">
      <text class="tag">{{ label(c.type) }}</text>
      <text class="title">{{ c.title }}</text>
      <view class="muted">{{ String(c.date || '').slice(0, 10) }} · {{ c.remark }}</view>
    </view>
    <view v-if="!list.length" class="muted empty">本月暂无事项（寿辰/忌日需管理员在前台客厅勾选成员）</view>
  </view>
</template>
<script setup lang="ts">
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { GenealogyShowcaseApi } from '@/api/genealogy'

const list = ref<any[]>([])
const label = (t: string) => ({ birthday: '寿辰', memorial: '忌日', festival: '节气', activity: '活动', scholarship: '资助' }[t] || t)
const mapLink = (link?: string) => {
  if (!link) return ''
  return link
    .replace('/portal/member?id=', '/pages/member/member?id=')
    .replace('/portal/activity?id=', '/pages/activity/activity?id=')
    .replace('/portal/ancestor', '/pages/ancestor/ancestor')
    .replace('/portal/scholarship', '/pages/scholarship/scholarship')
}
const go = (c: any) => {
  const url = mapLink(c.link)
  if (url) uni.navigateTo({ url })
}
onLoad(async () => {
  const d = new Date()
  try { list.value = (await GenealogyShowcaseApi.calendar(d.getFullYear(), d.getMonth() + 1)) || [] } catch {}
})
</script>
<style scoped>
.title { font-weight: 700; margin-left: 8rpx; }
.empty { text-align: center; padding: 80rpx 0; }
</style>
