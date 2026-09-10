<template>
  <view class="page">
    <view class="brand">
      <view class="seal">陈</view>
      <view>
        <view class="title">陈氏族谱</view>
        <view class="muted">族人登录后可查看族谱、申请资助、报名祭祖</view>
      </view>
    </view>
    <view class="card">
      <input class="input" v-model="username" placeholder="请输入账号" />
      <input class="input" v-model="password" password placeholder="请输入密码" />
      <button class="btn-primary" :loading="loading" @click="submit">登录</button>
      <button class="ghost" style="margin-top: 16rpx" @click="skip">暂不登录，先逛逛</button>
    </view>
  </view>
</template>
<script setup lang="ts">
import { ref } from 'vue'
import { onBackPress, onLoad } from '@dcloudio/uni-app'
import { useUserStore } from '@/store/user'
import { setTenantId } from '@/utils/auth'
import { AuthApi } from '@/api/login'
import { TAB_PAGES, goOpenHome } from '@/utils'

const username = ref('')
const password = ref('')
const loading = ref(false)
const redirect = ref('')

onLoad((q) => {
  redirect.value = decodeURIComponent((q?.redirect as string) || '')
})

onBackPress(() => {
  goOpenHome()
  return true
})

const skip = () => goOpenHome()

const afterLogin = () => {
  if (redirect.value && !redirect.value.includes('pages/login')) {
    if (TAB_PAGES.includes(redirect.value)) {
      uni.switchTab({ url: redirect.value })
      return
    }
    uni.redirectTo({ url: redirect.value })
    return
  }
  goOpenHome()
}

const submit = async () => {
  if (!username.value || !password.value) {
    uni.showToast({ title: '请输入账号和密码', icon: 'none' })
    return
  }
  loading.value = true
  try {
    if (import.meta.env.VITE_APP_TENANT_ENABLE === 'true') {
      try {
        const id = await AuthApi.getTenantIdByName('芋道源码')
        if (id) setTenantId(id)
      } catch {}
    }
    await useUserStore().login(username.value, password.value)
    uni.showToast({ title: '登录成功', icon: 'success' })
    afterLogin()
  } catch (e: any) {
    uni.showToast({ title: e?.message || '登录失败', icon: 'none' })
  } finally {
    loading.value = false
  }
}
</script>
<style scoped>
.brand { display: flex; align-items: center; gap: 24rpx; padding: 48rpx 8rpx 24rpx; }
.seal { width: 96rpx; height: 96rpx; border-radius: 20rpx; background: #a63d2f; color: #fff; display: flex; align-items: center; justify-content: center; font-size: 48rpx; font-weight: 700; }
.title { font-size: 40rpx; font-weight: 700; }
.input { background: #f4ece0; border-radius: 12rpx; padding: 22rpx 24rpx; margin-bottom: 24rpx; font-size: 28rpx; }
</style>
