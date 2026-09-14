<template>
  <view class="page">
    <view class="card" v-for="f in list" :key="f.id">
      <text class="tag">{{ feedType(f.type) }}</text>
      <text class="feed-title">{{ f.title }}</text>
      <view v-if="f.authorName" class="muted author">{{ f.authorName }}</view>
      <view class="muted content">{{ f.content }}</view>
      <view class="feed-actions">
        <text @click="like(f)">赞 {{ f.likeCount || 0 }}</text>
        <text @click="comment(f)">评论 {{ f.commentCount || 0 }}</text>
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
.empty { text-align: center; padding: 24rpx 0 48rpx; }
</style>
