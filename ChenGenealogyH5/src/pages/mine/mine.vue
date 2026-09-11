<template>
  <view class="page">
    <template v-if="logged">
      <view class="card">
        <view class="card-title">{{ member?.name || user?.nickname || '族人' }}</view>
        <view class="muted" v-if="member">{{ formatMemberGeneration(member) }}</view>
        <view class="muted" v-else>尚未关联族谱成员，请联系管理员认证</view>
        <button v-if="member?.id" class="btn-primary" style="margin-top: 20rpx" @click="go('/pages/member/member?id=' + member.id)">查看完整档案</button>
      </view>
      <view class="card" @click="go('/pages/scholarship/scholarship')">
        <view class="card-title">学海无涯</view>
        <view class="muted">资助申请 · 我的进度</view>
      </view>
      <view class="card" @click="go('/pages/feed/publish')">
        <view class="card-title">发布动态</view>
        <view class="muted">分享家族近况，发布后需管理员审核</view>
      </view>
      <view class="card">
        <view class="card-title">我的报名</view>
        <view v-for="r in regs" :key="r.id" class="row" @click="go('/pages/activity/activity?id=' + r.activityId)">
          活动 {{ r.activityId }} · {{ r.peopleCount }}人 · {{ r.status === 2 ? '候补' : '已报名' }}
        </view>
        <view v-if="!regs.length" class="muted">暂无报名记录</view>
      </view>
      <button class="ghost" @click="logout">退出登录</button>
    </template>
    <view v-else class="card">
      <view class="card-title">尚未登录</view>
      <view class="muted">登录后可查看族谱、申请资助、报名祭祖。也可以先逛逛首页和寻根问祖。</view>
      <button class="btn-primary" style="margin-top: 20rpx" @click="go('/pages/login/login')">去登录</button>
      <button class="ghost" style="margin-top: 16rpx" @click="goHome">暂不登录，先逛逛</button>
    </view>
  </view>
</template>
<script setup lang="ts">
import { computed, ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { GenealogyActivityApi } from '@/api/genealogy'
import { useUserStore } from '@/store/user'
import { formatMemberGeneration } from '@/utils'

const store = useUserStore()
const logged = computed(() => store.isLogin)
const member = computed(() => store.member)
const user = computed(() => store.user)
const regs = ref<any[]>([])
const go = (url: string) => uni.navigateTo({ url })
const goHome = () => uni.switchTab({ url: '/pages/index/index' })
const logout = async () => {
  await store.logout()
  regs.value = []
  uni.showToast({ title: '已退出', icon: 'none' })
  uni.switchTab({ url: '/pages/index/index' })
}

onShow(async () => {
  if (!logged.value) return
  await store.fetchInfo()
  try { regs.value = (await GenealogyActivityApi.myRegistrations()) || [] } catch {}
})
</script>
<style scoped>
.row { padding: 16rpx 0; border-bottom: 1px solid #e8dfcc; }
.ghost { background: #fffdf7; color: #a63d2f; border: 1px solid #e8dfcc; margin-top: 12rpx; }
</style>
