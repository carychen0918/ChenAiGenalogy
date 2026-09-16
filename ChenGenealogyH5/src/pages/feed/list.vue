<template>
  <view class="page">
    <view class="card" v-for="f in list" :key="f.id">
      <text class="tag">{{ feedType(f.type) }}</text>
      <text class="feed-title">{{ f.title }}</text>
      <view v-if="f.authorName" class="muted author">{{ f.authorName }}</view>
      <view class="muted content">{{ f.content }}</view>
      <view v-if="f.images?.length" class="photos">
        <image v-for="(img, i) in f.images" :key="img + i" :src="img" class="photo" mode="aspectFill" @click="preview(f.images, i)" />
      </view>
      <view class="feed-actions">
        <text @click="like(f)">赞 {{ f.likeCount || 0 }}</text>
        <text @click="toggleComments(f)">评论 {{ f.commentCount || 0 }}</text>
      </view>
      <view v-if="f._open" class="comments">
        <view v-for="c in f._comments || []" :key="c.id" class="comment">
          <text class="name">{{ c.userName || '族人' }}</text>
          <text>{{ c.content }}</text>
        </view>
        <view v-if="!(f._comments || []).length" class="muted">暂无已审评论</view>
        <button class="ghost" @click="comment(f)">写评论</button>
      </view>
    </view>
    <view class="muted empty">{{ footer }}</view>
  </view>
</template>
<script setup lang="ts">
import { computed, ref } from 'vue'
import { onReachBottom, onShow } from '@dcloudio/uni-app'
import { GenealogyFeedApi } from '@/api/genealogy'
import { isLoggedIn } from '@/utils/auth'
import { FEED_TYPE } from '@/utils'

const list = ref<any[]>([])
const pageNo = ref(1)
const pageSize = 10
const loading = ref(false)
const finished = ref(false)
const feedType = (t: number) => FEED_TYPE[t] || '动态'
const footer = computed(() => {
  if (loading.value) return '加载中…'
  if (!list.value.length) return '暂无动态'
  if (finished.value) return '已经到底了'
  return '上拉加载更多'
})

const ensureLogin = () => {
  if (isLoggedIn()) return true
  uni.navigateTo({ url: '/pages/login/login?redirect=' + encodeURIComponent('/pages/feed/list') })
  return false
}

const load = async () => {
  if (loading.value || finished.value) return
  loading.value = true
  try {
    const data: any = await GenealogyFeedApi.page({ pageNo: pageNo.value, pageSize, status: 1 })
    const rows = data?.list || []
    list.value = pageNo.value === 1 ? rows : list.value.concat(rows)
    if (!rows.length || list.value.length >= (data?.total || 0)) {
      finished.value = true
    } else {
      pageNo.value += 1
    }
  } catch {
    finished.value = true
  } finally {
    loading.value = false
  }
}

const fillPage = async () => {
  await load()
  if (finished.value) return
  await new Promise((r) => setTimeout(r, 50))
  const sys = uni.getSystemInfoSync()
  uni.createSelectorQuery()
    .select('.page')
    .boundingClientRect((rect: any) => {
      if (rect && rect.height <= (sys.windowHeight || 0) + 40) fillPage()
    })
    .exec()
}

onShow(() => {
  if (list.value.length) return
  finished.value = false
  pageNo.value = 1
  fillPage()
})
onReachBottom(() => load())

const preview = (urls: string[], i: number) => uni.previewImage({ urls, current: urls[i] })
const toggleComments = async (f: any) => {
  f._open = !f._open
  if (f._open && !f._comments) {
    try { f._comments = (await GenealogyFeedApi.comments(f.id)) || [] } catch { f._comments = [] }
  }
}

const like = async (f: any) => {
  if (!ensureLogin()) return
  await GenealogyFeedApi.like(f.id)
  f.likeCount = (f.likeCount || 0) + 1
}

const comment = (f: any) => {
  if (!ensureLogin()) return
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

</script>
<style scoped>
.feed-title { font-weight: 700; margin-left: 8rpx; }
.author { margin-top: 8rpx; }
.content { margin-top: 12rpx; white-space: pre-wrap; line-height: 1.6; }
.feed-actions { margin-top: 16rpx; color: #a63d2f; font-size: 24rpx; display: flex; gap: 32rpx; }
.photos { display: flex; flex-wrap: wrap; gap: 12rpx; margin-top: 12rpx; }
.photo { width: 160rpx; height: 160rpx; border-radius: 8rpx; background: #f4ece0; }
.comments { margin-top: 12rpx; background: #f8f3e8; border-radius: 12rpx; padding: 12rpx 16rpx; }
.comment { padding: 8rpx 0; font-size: 24rpx; }
.comment .name { font-weight: 700; margin-right: 8rpx; }
.ghost { margin-top: 12rpx; background: #fffdf7; color: #a63d2f; border: 1px solid #e8dfcc; font-size: 24rpx; }
.empty { text-align: center; padding: 24rpx 0 48rpx; }
</style>
