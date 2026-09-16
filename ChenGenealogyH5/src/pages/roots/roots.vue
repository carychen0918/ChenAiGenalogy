<template>
  <view class="page">
    <view class="card" @click="go('/pages/book/book')">
      <view class="card-title">谱书</view>
      <view class="muted">在线翻阅 · 逐辈分支 · 定位人与字辈</view>
    </view>
    <view class="card" @click="go('/pages/migration/migration')">
      <view class="card-title">姓氏源流与迁徙</view>
      <view class="muted">始祖溯源 · 迁徙路线 · 时间轴</view>
    </view>
    <view class="card" @click="go('/pages/generation/generation')">
      <view class="card-title">字辈 · 派语</view>
      <view class="muted">世代排行 · 新生儿取名推荐</view>
    </view>
    <view class="card" @click="go('/pages/ai-match/ai-match')">
      <view class="card-title">AI 寻宗问祖</view>
      <view class="muted">对话分析是否本支同族 · 匹配地区管理员</view>
    </view>
    <view class="card" @click="go('/pages/achievements/achievements')">
      <view class="card-title">祖先事迹</view>
      <view class="muted">先贤生平 · 家族记忆</view>
    </view>
    <view class="card" @click="go('/pages/gallery/gallery')">
      <view class="card-title">族影长廊</view>
      <view class="muted">成员照片 · 既有影像</view>
    </view>
    <view class="card" @click="go('/pages/calendar/calendar')">
      <view class="card-title">家族日历</view>
      <view class="muted">寿辰忌日 · 清明活动</view>
    </view>
    <view class="card" @click="go('/pages/search/search')">
      <view class="card-title">找族人</view>
      <view class="muted">姓名 · 几世 · 字辈</view>
    </view>
    <view class="card">
      <view class="card-title">姓氏源流简介</view>
      <view class="muted">{{ family.intro || '内容持续补充中' }}</view>
    </view>
  </view>
</template>
<script setup lang="ts">
import { ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { GenealogyContentApi } from '@/api/genealogy'

const family = ref<any>({})
const go = (url: string) => uni.navigateTo({ url })
onShow(async () => {
  try { family.value = await GenealogyContentApi.getFamily() } catch {}
})
</script>
