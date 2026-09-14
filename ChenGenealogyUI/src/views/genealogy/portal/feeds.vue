<template>
  <div class="feeds-page">
    <div class="head">
      <div>
        <div class="text-22px font-bold">家族动态</div>
        <div class="text-13px text-gray-500 mt-4px">公告、动态与公示均可浏览，点赞评论需登录</div>
      </div>
    </div>
    <el-card v-for="f in list" :key="f.id" class="mb-12px feed-card">
      <el-tag size="small">{{ feedType(f.type) }}</el-tag>
      <span class="ml-8px font-bold">{{ f.title }}</span>
      <div v-if="f.authorName" class="text-12px text-gray-400 mt-6px">{{ f.authorName }}</div>
      <div class="text-13px text-gray-500 mt-8px content">{{ f.content }}</div>
      <el-space class="mt-12px">
        <el-button link @click="like(f)">赞 {{ f.likeCount || 0 }}</el-button>
        <el-button link @click="comment(f)">评论 {{ f.commentCount || 0 }}</el-button>
      </el-space>
    </el-card>
    <el-empty v-if="!list.length && !loading" description="暂无动态" />
    <div ref="sentinelRef" class="sentinel">
      <span v-if="loading">加载中…</span>
      <span v-else-if="finished && list.length">已经到底了</span>
    </div>
  </div>
</template>
<script setup lang="ts">
import { GenealogyFeedApi } from '@/api/genealogy'
import { isLoggedIn, PORTAL_LOGIN } from '@/utils/portalAuth'

defineOptions({ name: 'PortalFeeds' })
const router = useRouter()
const message = useMessage()
const list = ref<any[]>([])
const pageNo = ref(1)
const pageSize = 10
const loading = ref(false)
const finished = ref(false)
const sentinelRef = ref<HTMLElement>()
const feedType = (t: number) => (t === 1 ? '公告' : t === 3 ? '公示' : '动态')

const ensureLogin = () => {
  if (isLoggedIn()) return true
  router.push(`${PORTAL_LOGIN}?redirect=${encodeURIComponent('/portal/feeds')}`)
  return false
}

const load = async () => {
  if (loading.value || finished.value) return
  loading.value = true
  try {
    const data = await GenealogyFeedApi.page({ pageNo: pageNo.value, pageSize, status: 1 })
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

const like = async (f: any) => {
  if (!ensureLogin()) return
  await GenealogyFeedApi.like(f.id)
  f.likeCount = (f.likeCount || 0) + 1
}

const comment = async (f: any) => {
  if (!ensureLogin()) return
  const { value } = await ElMessageBox.prompt('请输入评论', '评论')
  if (!value) return
  await GenealogyFeedApi.comment(f.id, value)
  message.success('评论已提交，等待审核')
}

let observer: IntersectionObserver | undefined
onMounted(async () => {
  await load()
  await nextTick()
  observer = new IntersectionObserver((entries) => {
    if (entries.some((e) => e.isIntersecting)) load()
  })
  if (sentinelRef.value) observer.observe(sentinelRef.value)
})
onUnmounted(() => observer?.disconnect())
</script>
<style scoped>
.feeds-page { max-width: 860px; }
.head { margin-bottom: 16px; }
.feed-card { background: #fffdf7; border: 1px solid #e8dfcc; }
.content { white-space: pre-wrap; line-height: 1.7; }
.sentinel { text-align: center; padding: 20px 0; color: #8b8273; font-size: 13px; }
</style>
