<template>
  <view class="page">
    <view class="hero">
      <view class="tag">{{ family.region || '宗族' }} · 数字平台</view>
      <view class="hero-title">陈氏族谱 · 寻根问祖</view>
      <view class="hero-desc">{{ family.intro || '溯源 · 传承 · 凝聚' }}</view>
      <view class="hero-btns">
        <button class="btn-primary hero-btn" @click="goTree">查看族谱</button>
        <button class="hero-btn ghost" @click="go('/pages/roots/roots')">寻根问祖</button>
      </view>
    </view>

    <swiper v-if="notices.length" class="notice-swiper" indicator-dots autoplay circular>
      <swiper-item v-for="n in notices" :key="n.id">
        <view class="card notice">
          <text class="tag">公告</text>
          <text class="notice-title">{{ n.title }}</text>
          <view class="muted">{{ n.content }}</view>
        </view>
      </swiper-item>
    </swiper>

    <view class="grid">
      <view class="grid-item card" v-for="m in modules" :key="m.url" @click="go(m.url)">
        <view class="grid-title">{{ m.title }}</view>
        <view class="muted">{{ m.desc }}</view>
      </view>
    </view>

    <view class="card">
      <view class="card-title">{{ logged ? '家族动态' : '文化指南' }}</view>
      <template v-if="logged">
        <view v-for="f in feeds" :key="f.id" class="feed">
          <text class="tag">{{ feedType(f.type) }}</text>
          <text class="feed-title">{{ f.title }}</text>
          <view class="muted">{{ f.content }}</view>
          <view class="feed-actions">
            <text @click="like(f)">赞 {{ f.likeCount || 0 }}</text>
            <text @click="comment(f)">评论 {{ f.commentCount || 0 }}</text>
          </view>
        </view>
        <view v-if="!feeds.length" class="muted empty">暂无动态</view>
      </template>
      <template v-else>
        <view v-for="c in cultures" :key="c.id" class="feed">
          <view class="feed-title">{{ c.title }}</view>
          <view class="muted">{{ strip(c.content) }}</view>
        </view>
        <view v-if="!cultures.length" class="muted empty">登录后可查看家族动态</view>
        <button class="btn-primary" style="margin-top: 16rpx" @click="go('/pages/login/login')">去登录</button>
      </template>
    </view>
  </view>
</template>
<script setup lang="ts">
import { computed, ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { GenealogyContentApi, GenealogyFeedApi } from '@/api/genealogy'
import { isLoggedIn } from '@/utils/auth'
import { FEED_TYPE, stripHtml } from '@/utils'

const family = ref<any>({})
const notices = ref<any[]>([])
const feeds = ref<any[]>([])
const cultures = ref<any[]>([])
const logged = computed(() => isLoggedIn())
const modules = [
  { title: '谱书', desc: '在线翻阅 · 按辈按支', url: '/pages/book/book' },
  { title: '基础族谱', desc: '谱系 · 成员档案', url: '/pages/tree/tree' },
  { title: '寻根问祖', desc: '源流 · 迁徙 · 字辈', url: '/pages/roots/roots' },
  { title: '学海无涯', desc: '家族助学申请', url: '/pages/scholarship/scholarship' }
]
const feedType = (t: number) => FEED_TYPE[t] || '动态'
const strip = (html: string) => stripHtml(html).slice(0, 48)
const TAB_PAGES = ['/pages/index/index', '/pages/tree/tree', '/pages/roots/roots', '/pages/ancestor/ancestor', '/pages/mine/mine']
const go = (url: string) => {
  if (TAB_PAGES.includes(url)) {
    uni.switchTab({ url })
    return
  }
  uni.navigateTo({ url })
}
const goTree = () => uni.switchTab({ url: '/pages/tree/tree' })
const like = async (f: any) => {
  await GenealogyFeedApi.like(f.id)
  f.likeCount = (f.likeCount || 0) + 1
}
const comment = (f: any) => {
  uni.showModal({
    title: '评论',
    editable: true,
    placeholderText: '请输入评论',
    success: async (res) => {
      if (!res.confirm || !res.content) return
      await GenealogyFeedApi.comment(f.id, res.content)
      uni.showToast({ title: '已提交，等待审核', icon: 'none' })
    }
  })
}

onShow(async () => {
  try { family.value = await GenealogyContentApi.getFamily() } catch {}
  try { notices.value = (await GenealogyFeedApi.page({ pageNo: 1, pageSize: 5, type: 1, status: 1 })).list || [] } catch {}
  if (logged.value) {
    try { feeds.value = (await GenealogyFeedApi.page({ pageNo: 1, pageSize: 8, status: 1 })).list || [] } catch {}
  } else {
    try { cultures.value = await GenealogyContentApi.cultureList() } catch {}
  }
})
</script>
<style scoped>
.hero { background: linear-gradient(135deg, #3a3126, #7a5c36); color: #f6f1e6; border-radius: 20rpx; padding: 48rpx 36rpx; margin-bottom: 24rpx; }
.hero-title { font-size: 40rpx; font-weight: 700; margin: 20rpx 0 12rpx; }
.hero-desc { font-size: 26rpx; opacity: .85; line-height: 1.6; }
.hero-btns { display: flex; gap: 16rpx; margin-top: 28rpx; }
.hero-btn { flex: 1; font-size: 26rpx; }
.ghost { background: transparent; color: #f6f1e6; border: 1px solid rgba(246,241,230,.4); }
.notice-swiper { height: 180rpx; margin-bottom: 24rpx; }
.notice-title { font-weight: 700; margin-left: 12rpx; }
.grid { display: flex; flex-wrap: wrap; gap: 16rpx; margin-bottom: 24rpx; }
.grid-item { width: calc(50% - 8rpx); margin-bottom: 0; box-sizing: border-box; }
.grid-title { font-size: 30rpx; font-weight: 700; margin-bottom: 8rpx; }
.feed { padding: 20rpx 0; border-bottom: 1px solid #e8dfcc; }
.feed-title { font-weight: 700; margin-left: 8rpx; }
.feed-actions { margin-top: 12rpx; color: #a63d2f; font-size: 24rpx; display: flex; gap: 32rpx; }
.empty { text-align: center; padding: 24rpx 0; }
</style>
