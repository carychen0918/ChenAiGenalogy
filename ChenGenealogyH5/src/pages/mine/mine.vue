<template>
  <view class="page">
    <template v-if="logged">
      <view class="card">
        <view class="card-title">{{ member?.name || user?.nickname || '族人' }}</view>
        <view class="muted" v-if="member">{{ formatMemberGeneration(member) }}</view>
        <view class="muted" v-else>尚未关联族谱成员，请联系管理员认证</view>
        <view v-if="mini.ancestors?.length" class="seat">上三代 {{ mini.ancestors.map((a: any) => a.name).join(' → ') }}</view>
        <view v-if="mini.mother" class="seat">母亲 {{ mini.mother.name }}</view>
        <view v-if="(mini.children || []).length" class="seat">子女 {{ mini.children.map((c: any) => c.name).join('、') }}</view>
        <button v-if="member?.id" class="btn-primary" style="margin-top: 20rpx" @click="go('/pages/member/member?id=' + member.id)">查看完整档案</button>
        <button v-if="member?.id" class="ghost" @click="goTree">在族谱中定位</button>
      </view>
      <view class="card" v-if="upcoming.length">
        <view class="card-title">近期日历</view>
        <view v-for="c in upcoming" :key="c.type + c.title + c.date" class="row" @click="goCal(c)">
          {{ c.title }} · {{ String(c.date || '').slice(0, 10) }}
        </view>
      </view>
      <view class="card" @click="go('/pages/scholarship/scholarship')">
        <view class="card-title">学海无涯</view>
        <view class="muted">资助申请 · 我的进度</view>
      </view>
      <view class="card" @click="go('/pages/search/search')">
        <view class="card-title">找族人</view>
        <view class="muted">姓名 · 几世 · 字辈</view>
      </view>
      <view class="card" @click="go('/pages/calendar/calendar')">
        <view class="card-title">家族日历</view>
        <view class="muted">寿辰忌日由管理员勾选成员后展示 · 清明活动</view>
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
import { GenealogyActivityApi, GenealogyShowcaseApi } from '@/api/genealogy'
import { useUserStore } from '@/store/user'
import { formatMemberGeneration } from '@/utils'

const store = useUserStore()
const logged = computed(() => store.isLogin)
const member = computed(() => store.member)
const user = computed(() => store.user)
const regs = ref<any[]>([])
const mini = ref<any>({})
const upcoming = ref<any[]>([])
const go = (url: string) => uni.navigateTo({ url })
const goHome = () => uni.switchTab({ url: '/pages/index/index' })
const goTree = () => uni.switchTab({ url: '/pages/tree/tree' })
const goCal = (c: any) => {
  if (c.link?.includes('/portal/activity')) {
    go('/pages/activity/activity?id=' + (c.refId || ''))
    return
  }
  go('/pages/calendar/calendar')
}
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
  if (member.value?.id) {
    try { mini.value = (await GenealogyShowcaseApi.miniFamily(member.value.id)) || {} } catch { mini.value = {} }
  }
  try {
    const now = new Date().toISOString().slice(0, 10)
    upcoming.value = ((await GenealogyShowcaseApi.calendar()) || [])
      .filter((c: any) => String(c.date || '').slice(0, 10) >= now)
      .slice(0, 5)
  } catch { upcoming.value = [] }
})
</script>
<style scoped>
.row { padding: 16rpx 0; border-bottom: 1px solid #e8dfcc; }
.ghost { background: #fffdf7; color: #a63d2f; border: 1px solid #e8dfcc; margin-top: 12rpx; }
.seat { font-size: 24rpx; color: #5c5348; margin-top: 8rpx; }
</style>
