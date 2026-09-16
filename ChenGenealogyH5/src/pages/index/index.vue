<template>
  <view class="page">
    <view class="hero">
      <view class="tag">{{ family.region || '宗族' }} · 宗族客厅</view>
      <view class="hero-title">陈氏族谱 · 寻根问祖</view>
      <view class="hero-desc">{{ family.intro || '溯源 · 传承 · 凝聚' }}</view>
      <view class="hero-btns">
        <button class="btn-primary hero-btn" @click="goTree">查看族谱</button>
        <button class="hero-btn ghost" @click="go('/pages/gallery/gallery')">族影长廊</button>
      </view>
    </view>

    <view class="search-entry" @click="go('/pages/search/search')">
      <text>搜姓名 / 几世 / 字辈</text>
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

    <view class="card person" v-if="home.featuredPerson" @click="goMember(home.featuredPerson.id)">
      <view class="muted">本周人物 · {{ home.featuredPerson.source }}</view>
      <view class="person-row">
        <image v-if="home.featuredPerson.photo || home.featuredPerson.avatar" class="avatar" :src="home.featuredPerson.photo || home.featuredPerson.avatar" mode="aspectFill" />
        <view>
          <view class="card-title">{{ home.featuredPerson.name }}</view>
          <view class="muted">{{ genLabel(home.featuredPerson) }} · {{ home.featuredPerson.lifeSpan }}</view>
          <view class="muted intro">{{ home.featuredPerson.intro || '生平待补充' }}</view>
        </view>
      </view>
    </view>

    <view class="card" v-if="(home.gallery || []).length">
      <view class="card-head">
        <view class="card-title">族影长廊</view>
        <text class="more" @click="go('/pages/gallery/gallery')">全部</text>
      </view>
      <view class="photos">
        <image v-for="(g, i) in home.gallery" :key="g.url + i" :src="g.url" class="photo" mode="aspectFill" @click="preview(home.gallery, i)" />
      </view>
    </view>

    <view class="grid">
      <view class="grid-item card" v-for="m in modules" :key="m.url" @click="go(m.url)">
        <view class="grid-title">{{ m.title }}</view>
        <view class="muted">{{ m.desc }}</view>
      </view>
    </view>

    <view class="card">
      <view class="card-head">
        <view class="card-title">家族日历</view>
        <text class="more" @click="go('/pages/calendar/calendar')">本月</text>
      </view>
      <view v-for="c in home.calendar || []" :key="c.type + c.title + c.date" class="feed" @click="goCal(c)">
        <text class="tag">{{ calType(c.type) }}</text>
        <text class="feed-title">{{ c.title }}</text>
        <view class="muted">{{ String(c.date || '').slice(0, 10) }} · {{ c.remark }}</view>
      </view>
      <view v-if="!(home.calendar || []).length" class="muted empty">近期暂无纪念日</view>
    </view>

    <view class="card">
      <view class="card-head">
        <view class="card-title">助学榜样</view>
        <text class="more" @click="go('/pages/scholarship/scholarship')">学海无涯</text>
      </view>
      <view v-for="w in home.scholarshipWall || []" :key="w.applicationId" class="feed">
        <view class="feed-title">{{ w.name }} · {{ w.year }}</view>
        <view class="muted">{{ [w.school, w.major, w.grade].filter(Boolean).join(' · ') }}</view>
      </view>
      <view v-if="!(home.scholarshipWall || []).length" class="muted empty">榜样墙将在发放后展示</view>
    </view>

    <view class="card">
      <view class="card-head">
        <view class="card-title">家族动态</view>
        <text v-if="(home.feeds || []).length" class="more" @click="go('/pages/feed/list')">更多</text>
      </view>
      <view v-for="f in home.feeds || []" :key="f.id" class="feed">
        <text class="tag">{{ feedType(f.type) }}</text>
        <text class="feed-title">{{ f.title }}</text>
        <view class="muted">{{ f.content }}</view>
        <view v-if="f.images?.length" class="photos">
          <image v-for="(img, i) in f.images" :key="img + i" :src="img" class="photo" mode="aspectFill" @click="previewImgs(f.images, i)" />
        </view>
        <view class="feed-actions">
          <text @click="like(f)">赞 {{ f.likeCount || 0 }}</text>
          <text @click="go('/pages/feed/list')">评论 {{ f.commentCount || 0 }}</text>
        </view>
      </view>
      <view v-if="!(home.feeds || []).length" class="muted empty">暂无动态</view>
    </view>
  </view>
</template>
<script setup lang="ts">
import { ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { GenealogyContentApi, GenealogyFeedApi, GenealogyShowcaseApi } from '@/api/genealogy'
import { isLoggedIn } from '@/utils/auth'
import { FEED_TYPE } from '@/utils'

const family = ref<any>({})
const notices = ref<any[]>([])
const home = ref<any>({})
const modules = [
  { title: '谱书', desc: '在线翻阅 · 按辈按支', url: '/pages/book/book' },
  { title: '基础族谱', desc: '谱系 · 成员档案', url: '/pages/tree/tree' },
  { title: '寻根问祖', desc: '源流 · 迁徙 · 字辈', url: '/pages/roots/roots' },
  { title: '学海无涯', desc: '榜样墙 · 资助申请', url: '/pages/scholarship/scholarship' },
  { title: '家族日历', desc: '寿辰 · 忌日 · 活动', url: '/pages/calendar/calendar' },
  { title: '族影长廊', desc: '既有照片汇聚', url: '/pages/gallery/gallery' }
]
const feedType = (t: number) => FEED_TYPE[t] || '动态'
const calType = (t: string) => ({ birthday: '寿辰', memorial: '忌日', festival: '节气', activity: '活动', scholarship: '资助' }[t] || t)
const genLabel = (p: any) => [p.generationNo ? p.generationNo + '世' : '', p.generationWord ? p.generationWord + '字辈' : ''].filter(Boolean).join(' · ')
const TAB_PAGES = ['/pages/index/index', '/pages/tree/tree', '/pages/roots/roots', '/pages/ancestor/ancestor', '/pages/mine/mine']
const go = (url: string) => {
  if (TAB_PAGES.includes(url)) {
    uni.switchTab({ url })
    return
  }
  uni.navigateTo({ url })
}
const goTree = () => uni.switchTab({ url: '/pages/tree/tree' })
const goMember = (id?: number) => id && uni.navigateTo({ url: '/pages/member/member?id=' + id })
const mapLink = (link?: string) => {
  if (!link) return ''
  return link
    .replace('/portal/member?id=', '/pages/member/member?id=')
    .replace('/portal/activity?id=', '/pages/activity/activity?id=')
    .replace('/portal/ancestor', '/pages/ancestor/ancestor')
    .replace('/portal/scholarship', '/pages/scholarship/scholarship')
}
const goCal = (c: any) => {
  const url = mapLink(c.link)
  if (url) go(url)
}
const preview = (list: any[], i: number) => {
  uni.previewImage({ urls: list.map((x) => x.url), current: list[i]?.url })
}
const previewImgs = (urls: string[], i: number) => {
  uni.previewImage({ urls, current: urls[i] })
}
const ensureLogin = () => {
  if (isLoggedIn()) return true
  uni.navigateTo({ url: '/pages/login/login?redirect=' + encodeURIComponent('/pages/index/index') })
  return false
}
const like = async (f: any) => {
  if (!ensureLogin()) return
  await GenealogyFeedApi.like(f.id)
  f.likeCount = (f.likeCount || 0) + 1
}

onShow(async () => {
  try { family.value = await GenealogyContentApi.getFamily() } catch {}
  try { home.value = (await GenealogyShowcaseApi.home()) || {} } catch { home.value = {} }
  try { notices.value = (await GenealogyFeedApi.page({ pageNo: 1, pageSize: 5, type: 1, status: 1 })).list || [] } catch {}
})
</script>
<style scoped>
.hero { background: linear-gradient(135deg, #3a3126, #7a5c36); color: #f6f1e6; border-radius: 20rpx; padding: 48rpx 36rpx; margin-bottom: 24rpx; }
.hero-title { font-size: 40rpx; font-weight: 700; margin: 20rpx 0 12rpx; }
.hero-desc { font-size: 26rpx; opacity: .85; line-height: 1.6; }
.hero-btns { display: flex; gap: 16rpx; margin-top: 28rpx; }
.hero-btn { flex: 1; font-size: 26rpx; }
.ghost { background: transparent; color: #f6f1e6; border: 1px solid rgba(246,241,230,.4); }
.search-entry {
  background: #fffdf7;
  border: 1px solid #e8dfcc;
  border-radius: 16rpx;
  padding: 22rpx 24rpx;
  color: #8b8273;
  margin-bottom: 24rpx;
}
.notice-swiper { height: 180rpx; margin-bottom: 24rpx; }
.notice-title { font-weight: 700; margin-left: 12rpx; }
.grid { display: flex; flex-wrap: wrap; gap: 16rpx; margin-bottom: 24rpx; }
.grid-item { width: calc(50% - 8rpx); margin-bottom: 0; box-sizing: border-box; }
.grid-title { font-size: 30rpx; font-weight: 700; margin-bottom: 8rpx; }
.feed { padding: 20rpx 0; border-bottom: 1px solid #e8dfcc; }
.feed-title { font-weight: 700; margin-left: 8rpx; }
.feed-actions { margin-top: 12rpx; color: #a63d2f; font-size: 24rpx; display: flex; gap: 32rpx; }
.empty { text-align: center; padding: 24rpx 0; }
.card-head { display: flex; align-items: center; justify-content: space-between; margin-bottom: 8rpx; }
.more { color: #a63d2f; font-size: 26rpx; }
.person-row { display: flex; gap: 20rpx; margin-top: 12rpx; }
.avatar { width: 140rpx; height: 168rpx; border-radius: 12rpx; background: #f4ece0; }
.intro { margin-top: 8rpx; }
.photos { display: flex; flex-wrap: wrap; gap: 12rpx; margin-top: 12rpx; }
.photo { width: 140rpx; height: 140rpx; border-radius: 8rpx; background: #f4ece0; }
</style>
