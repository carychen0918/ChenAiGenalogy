<template>
  <view class="page">
    <guest-login v-if="!logged" title="申请资助需登录" />
    <template v-else>
      <view class="card">
        <view class="card-title">学海无涯</view>
        <view class="muted">资助优秀学子，助力陈氏后人求学。申请后按地区逐级审核。</view>
        <view v-if="config" class="muted" style="margin-top: 12rpx">
          申请窗口：{{ config.open ? '开放中' : '未开放' }}
          <text v-if="config.windowStart"> · {{ formatDate(config.windowStart) }} 至 {{ formatDate(config.windowEnd) }}</text>
        </view>
        <button class="btn-primary" style="margin-top: 20rpx" :disabled="config && !config.open" @click="go('/pages/scholarship/apply')">申请资助</button>
        <button class="ghost" @click="go('/pages/scholarship/mine')">我的申请</button>
      </view>
      <view class="card">
        <view class="card-title">资助公告</view>
        <view v-for="n in notices" :key="n.id" class="row" @click="open(n)">
          <view class="name">{{ n.title }}</view>
          <view class="muted">{{ n.content }}</view>
        </view>
        <view v-if="!notices.length" class="muted empty">暂无公告</view>
      </view>
    </template>
  </view>
</template>
<script setup lang="ts">
import { ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import GuestLogin from '@/components/guest-login.vue'
import { GenealogyFeedApi, GenealogyScholarshipApi } from '@/api/genealogy'
import { isLoggedIn } from '@/utils/auth'
import { formatDate } from '@/utils'

const logged = ref(isLoggedIn())
const notices = ref<any[]>([])
const config = ref<any>()
const go = (url: string) => uni.navigateTo({ url })
const open = (n: any) => {
  uni.showModal({ title: n.title, content: n.content || '暂无内容', showCancel: false })
}
onShow(async () => {
  logged.value = isLoggedIn()
  if (!logged.value) return
  try { config.value = await GenealogyScholarshipApi.getConfig() } catch {}
  try { notices.value = (await GenealogyFeedApi.page({ pageNo: 1, pageSize: 20, type: 1, status: 1 })).list || [] } catch {}
})
</script>
<style scoped>
.row { padding: 16rpx 0; border-bottom: 1px solid #e8dfcc; }
.name { font-weight: 700; }
.ghost { background: #fffdf7; color: #a63d2f; border: 1px solid #e8dfcc; margin-top: 16rpx; }
.empty { text-align: center; padding: 24rpx 0; }
</style>
