<template>
  <div>
    <section class="hero">
      <div class="tag">{{ family.region }} · 宗族数字平台</div>
      <h1>陈氏族谱 · 寻根问祖</h1>
      <p>{{ family.intro }}</p>
      <div class="mt-24px flex gap-12px">
        <el-button type="primary" size="large" @click="$router.push(logged ? '/portal/tree' : '/portal/login?redirect=/portal/tree')">查看族谱</el-button>
        <el-button size="large" @click="$router.push('/portal/roots')">寻根问祖</el-button>
        <el-button v-if="!logged" size="large" @click="$router.push('/portal/login')">族人登录</el-button>
      </div>
    </section>
    <el-carousel v-if="notices.length" height="120px" class="mb-24px" indicator-position="outside">
      <el-carousel-item v-for="n in notices" :key="n.id">
        <div class="notice">
          <el-tag size="small">公告</el-tag>
          <span class="ml-8px font-bold">{{ n.title }}</span>
          <div class="text-13px mt-6px">{{ n.content }}</div>
        </div>
      </el-carousel-item>
    </el-carousel>
    <el-row :gutter="16" class="mb-24px">
      <el-col :span="6" v-for="m in modules" :key="m.path">
        <el-card shadow="hover" class="cursor-pointer" @click="$router.push(m.path)">
          <div class="text-16px font-bold">{{ m.title }}</div>
          <div class="text-12px text-gray-500 mt-6px">{{ m.desc }}</div>
        </el-card>
      </el-col>
    </el-row>
    <el-row :gutter="16">
      <el-col :span="12">
        <el-card>
          <template #header>
            <div class="flex items-center justify-between">
              <span>家族动态</span>
              <el-button v-if="feedTotal > 10" link type="primary" @click="$router.push('/portal/feeds')">更多</el-button>
            </div>
          </template>
          <div v-for="f in feeds" :key="f.id" class="py-12px border-b">
            <el-tag size="small">{{ f.type === 1 ? '公告' : f.type === 3 ? '公示' : '动态' }}</el-tag>
            <span class="ml-8px font-bold">{{ f.title }}</span>
            <div class="text-13px text-gray-500 mt-4px">{{ f.content }}</div>
            <el-space class="mt-8px">
              <el-button link @click="like(f)">赞 {{ f.likeCount || 0 }}</el-button>
              <el-button link @click="comment(f)">评论 {{ f.commentCount || 0 }}</el-button>
            </el-space>
          </div>
          <el-empty v-if="!feeds.length" description="暂无动态" />
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card>
          <template #header>{{ logged ? '资助窗口' : '文化指南' }}</template>
          <template v-if="logged">
          <div>{{ config?.open ? '开放中' : '未开放' }}</div>
          <div class="text-13px text-gray-500 mt-8px" v-if="config">申请时间：{{ format(config.windowStart) }} 至 {{ format(config.windowEnd) }}</div>
          <el-button class="mt-12px" type="primary" :disabled="!config?.open" @click="$router.push('/portal/scholarship-apply')">立即申请</el-button>
          </template>
          <template v-else>
            <div v-for="c in cultures" :key="c.id" class="py-8px border-b">
              <div class="font-bold">{{ c.title }}</div>
              <div class="text-12px text-gray-500">{{ (c.content || '').replace(/<[^>]+>/g, '').slice(0, 48) }}</div>
            </div>
            <el-empty v-if="!cultures.length" description="文化内容持续补充中" />
          </template>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>
<script setup lang="ts">
import { GenealogyContentApi, GenealogyFeedApi, GenealogyScholarshipApi } from '@/api/genealogy'
import { isLoggedIn, PORTAL_LOGIN } from '@/utils/portalAuth'
defineOptions({ name: 'PortalHome' })
const router = useRouter()
const message = useMessage()
const logged = computed(() => isLoggedIn())
const family = ref<any>({})
const feeds = ref<any[]>([])
const feedTotal = ref(0)
const notices = ref<any[]>([])
const cultures = ref<any[]>([])
const config = ref<any>()
const modules = [
  { title: '谱书', desc: '在线翻阅 · 按辈按支', path: '/portal/book' },
  { title: '基础族谱', desc: '谱系树 · 成员档案', path: '/portal/tree' },
  { title: '寻根问祖', desc: '姓氏源流 · 迁徙 · 字辈', path: '/portal/roots' },
  { title: '清明祭祖', desc: '活动报名 · 坟地导航', path: '/portal/ancestor' }
]
const format = (t: number) => (t ? new Date(t).toLocaleDateString() : '')
const ensureLogin = (redirect = '/portal/home') => {
  if (isLoggedIn()) return true
  router.push(`${PORTAL_LOGIN}?redirect=${encodeURIComponent(redirect)}`)
  return false
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
onMounted(async () => {
  try { family.value = await GenealogyContentApi.getFamily() } catch {}
  try { notices.value = (await GenealogyFeedApi.page({ pageNo: 1, pageSize: 5, type: 1, status: 1 })).list } catch {}
  try {
    const data = await GenealogyFeedApi.page({ pageNo: 1, pageSize: 10, status: 1 })
    feeds.value = data?.list || []
    feedTotal.value = data?.total || feeds.value.length
  } catch {}
  if (logged.value) {
    try { config.value = await GenealogyScholarshipApi.getConfig() } catch {}
  } else {
    try { cultures.value = await GenealogyContentApi.cultureList() } catch {}
  }
})
</script>
<style scoped>
.hero { position: relative; border-radius: 14px; overflow: hidden; background: linear-gradient(135deg, #3a3126 0%, #55462f 55%, #7a5c36 100%); color: #f6f1e6; padding: 52px 40px; margin-bottom: 28px; }
.hero h1 { font-size: 34px; margin: 16px 0 10px; }
.tag { display: inline-block; border: 1px solid rgba(246,241,230,.3); padding: 4px 14px; border-radius: 999px; font-size: 12px; }
.notice { background: #fffdf7; border: 1px solid #e8dfcc; border-radius: 12px; padding: 18px 22px; height: 100%; }
</style>
