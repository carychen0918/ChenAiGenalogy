<template>
  <view class="page">
    <guest-login v-if="!logged" title="发布动态需登录" />
    <view class="card" v-else>
      <view class="card-title">发布动态</view>
      <input class="input" v-model="title" placeholder="标题" />
      <textarea class="input area" v-model="content" placeholder="分享家族近况，发布后需管理员审核" />
      <view class="imgs">
        <image v-for="(u, i) in urls" :key="i" :src="u" class="img" mode="aspectFill" />
        <view v-if="urls.length < 9" class="add" @click="choose">+</view>
      </view>
      <button class="btn-primary" :loading="loading" @click="submit">提交审核</button>
    </view>
  </view>
</template>
<script setup lang="ts">
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import GuestLogin from '@/components/guest-login.vue'
import { GenealogyFeedApi } from '@/api/genealogy'
import { upload } from '@/utils/request'
import { isLoggedIn } from '@/utils/auth'

const logged = ref(isLoggedIn())
const title = ref('')
const content = ref('')
const urls = ref<string[]>([])
const loading = ref(false)

onLoad(() => {
  logged.value = isLoggedIn()
})
const choose = () => {
  uni.chooseImage({
    count: 9 - urls.value.length,
    success: async (res) => {
      for (const p of res.tempFilePaths) {
        try {
          urls.value.push(await upload(p))
        } catch (e: any) {
          uni.showToast({ title: e?.message || '上传失败', icon: 'none' })
        }
      }
    }
  })
}
const submit = async () => {
  if (!title.value.trim() || !content.value.trim()) {
    uni.showToast({ title: '请填写标题和内容', icon: 'none' })
    return
  }
  loading.value = true
  try {
    await GenealogyFeedApi.create({ type: 2, title: title.value, content: content.value, images: urls.value })
    uni.showToast({ title: '已提交，等待审核', icon: 'success' })
    setTimeout(() => uni.switchTab({ url: '/pages/index/index' }), 800)
  } catch (e: any) {
    uni.showToast({ title: e?.message || '提交失败', icon: 'none' })
  } finally {
    loading.value = false
  }
}
</script>
<style scoped>
.input { background: #f4ece0; border-radius: 12rpx; padding: 22rpx 24rpx; margin-bottom: 20rpx; }
.area { height: 200rpx; }
.imgs { display: flex; flex-wrap: wrap; gap: 16rpx; margin-bottom: 24rpx; }
.img, .add { width: 160rpx; height: 160rpx; border-radius: 12rpx; background: #f4ece0; }
.add { display: flex; align-items: center; justify-content: center; font-size: 64rpx; color: #8a8174; }
</style>
