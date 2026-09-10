<template>
  <view class="page">
    <guest-login v-if="!logged" title="祭祖功能需登录" desc="登录后可报名活动、导航坟地。文化指南可直接阅读，也可先逛逛首页和寻根问祖。" />
    <view class="card" v-if="logged">
      <view class="card-title">{{ tomb?.name || '温蒂坟地' }}</view>
      <view class="muted">{{ tomb?.address || '地点信息待管理员配置' }}</view>
      <button class="btn-primary" style="margin-top: 20rpx" :disabled="!canNav" @click="nav">
        {{ canNav ? '导航前往' : '地点信息待管理员配置' }}
      </button>
    </view>
    <view class="card">
      <view class="card-title">祭祖文化指南</view>
      <view v-for="c in cultures" :key="c.id" class="row" @click="openGuide(c)">
        <view class="name">{{ c.title }}</view>
        <view class="muted">{{ strip(c.content) }}</view>
      </view>
      <view v-if="!cultures.length" class="muted empty">指南待补充</view>
    </view>
    <template v-if="logged">
      <view class="card">
        <view class="card-title">祭祖活动</view>
        <view v-for="a in activities" :key="a.id" class="row between" @click="goAct(a.id)">
          <view>
            <view class="name">{{ a.title }}</view>
            <view class="muted">{{ a.place }} · {{ formatDate(a.startTime) }}</view>
          </view>
          <text class="link">详情</text>
        </view>
        <view v-if="!activities.length" class="muted empty">暂无祭祖活动</view>
      </view>
      <view class="card">
        <view class="card-title">祭扫记录</view>
        <view v-for="w in worships" :key="w.id" class="row">
          <text v-if="w.pinned" class="tag">精选</text>
          <text class="name">{{ w.userName }}</text>
          <text v-if="w.ancestorName" class="muted"> · 致 {{ w.ancestorName }}</text>
          <view class="muted">{{ w.content }}</view>
        </view>
        <view v-if="!worships.length" class="muted empty">暂无祭扫记录</view>
      </view>
    </template>
  </view>
</template>
<script setup lang="ts">
import { computed, ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import GuestLogin from '@/components/guest-login.vue'
import { GenealogyActivityApi, GenealogyContentApi } from '@/api/genealogy'
import { isLoggedIn } from '@/utils/auth'
import { formatDate, requireLogin, stripHtml } from '@/utils'

const logged = ref(isLoggedIn())
const tomb = ref<any>()
const cultures = ref<any[]>([])
const activities = ref<any[]>([])
const worships = ref<any[]>([])
const canNav = computed(() => tomb.value?.longitude && tomb.value?.latitude)
const strip = (html: string) => stripHtml(html).slice(0, 40)
const nav = () => {
  if (!requireLogin()) return
  uni.navigateTo({ url: '/pages/nav/nav' })
}
const goAct = (id: number) => {
  if (!requireLogin()) return
  uni.navigateTo({ url: '/pages/activity/activity?id=' + id })
}
const openGuide = (c: any) => {
  uni.showModal({ title: c.title, content: stripHtml(c.content) || '暂无内容', showCancel: false })
}

onShow(async () => {
  logged.value = isLoggedIn()
  try { cultures.value = await GenealogyContentApi.cultureList() } catch {}
  if (!logged.value) {
    tomb.value = undefined
    activities.value = []
    worships.value = []
    return
  }
  try { tomb.value = await GenealogyContentApi.getTomb() } catch {}
  try { activities.value = (await GenealogyActivityApi.page({ pageNo: 1, pageSize: 20 })).list || [] } catch {}
  try { worships.value = (await GenealogyActivityApi.worshipList()) || [] } catch {}
})
</script>
<style scoped>
.row { padding: 16rpx 0; border-bottom: 1px solid #e8dfcc; }
.between { display: flex; justify-content: space-between; align-items: center; }
.name { font-weight: 700; }
.link { color: #a63d2f; font-size: 24rpx; }
.empty { text-align: center; padding: 24rpx 0; }
</style>
