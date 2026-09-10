<template>
  <view class="card">
    <view class="card-title">{{ title }}</view>
    <view class="muted">{{ desc }}</view>
    <button class="btn-primary" style="margin-top: 20rpx" @click="toLogin">去登录</button>
    <button class="ghost" style="margin-top: 16rpx" @click="browse">暂不登录，先逛逛</button>
  </view>
</template>
<script setup lang="ts">
withDefaults(defineProps<{ title?: string; desc?: string }>(), {
  title: '需要登录',
  desc: '登录后可查看族谱、申请资助、报名祭祖。也可以先浏览寻根、AI 寻宗等内容。'
})
const toLogin = () => uni.navigateTo({ url: '/pages/login/login' })
const browse = () => uni.switchTab({ url: '/pages/index/index' })
</script>
