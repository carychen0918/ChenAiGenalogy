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
      <div v-if="f.images?.length" class="mt-10px flex flex-wrap gap-8px">
        <el-image
          v-for="(img, i) in f.images"
          :key="img + i"
          :src="img"
          class="w-88px h-88px"
          fit="cover"
          preview-teleported
          :preview-src-list="f.images"
          :initial-index="i"
        />
      </div>
      <el-space class="mt-12px">
        <el-button link @click="like(f)">赞 {{ f.likeCount || 0 }}</el-button>
        <el-button link @click="toggleComments(f)">评论 {{ f.commentCount || 0 }}</el-button>
      </el-space>
      <div v-if="f._open" class="comments">
        <div v-for="c in f._comments || []" :key="c.id" class="comment">
          <span class="font-bold">{{ c.userName || '族人' }}</span>
          <span class="ml-8px">{{ c.content }}</span>
        </div>
        <el-empty v-if="!(f._comments || []).length" description="暂无已审评论" />
        <el-input v-if="logged" v-model="f._draft" placeholder="写下你的评论" class="mt-8px" @keyup.enter="comment(f)">
          <template #append>
            <el-button @click="comment(f)">发送</el-button>
          </template>
        </el-input>
        <div v-else class="text-12px text-gray-400 mt-8px">登录后可评论</div>
      </div>
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
const logged = computed(() => isLoggedIn())
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
    const data = await GenealogyFeedApi.frontPage({ pageNo: pageNo.value, pageSize, status: 1 })
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

const toggleComments = async (f: any) => {
  f._open = !f._open
  if (f._open && !f._comments) {
    f._comments = (await GenealogyFeedApi.comments(f.id)) || []
  }
}

const like = async (f: any) => {
  if (!ensureLogin()) return
  await GenealogyFeedApi.like(f.id)
  f.likeCount = (f.likeCount || 0) + 1
}

const comment = async (f: any) => {
  if (!ensureLogin()) return
  const text = (f._draft || '').trim()
  if (!text) {
    const { value } = await ElMessageBox.prompt('请输入评论', '评论')
    if (!value) return
    f._draft = value
  }
  const content = (f._draft || '').trim()
  if (!content) return
  await GenealogyFeedApi.comment(f.id, content)
  f._draft = ''
  f.commentCount = (f.commentCount || 0) + 1
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
.comments { margin-top: 12px; background: #f8f3e8; border-radius: 8px; padding: 10px 12px; }
.comment { padding: 6px 0; font-size: 13px; border-bottom: 1px dashed #eadfcb; }
.sentinel { text-align: center; padding: 20px 0; color: #8b8273; font-size: 13px; }
</style>
