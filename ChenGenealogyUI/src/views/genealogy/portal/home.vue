<template>
  <div class="home">
    <section class="hero">
      <div class="tag">{{ family.region }} · 宗族客厅</div>
      <h1>陈氏族谱 · 寻根问祖</h1>
      <p class="intro">{{ family.intro }}</p>
      <div class="hero-actions">
        <el-button type="primary" size="large" @click="$router.push(logged ? '/portal/tree' : '/portal/login?redirect=/portal/tree')">查看族谱</el-button>
        <el-button size="large" @click="$router.push('/portal/gallery')">族影长廊</el-button>
        <el-button size="large" @click="$router.push('/portal/calendar')">家族日历</el-button>
        <el-button v-if="!logged" size="large" @click="$router.push('/portal/login')">族人登录</el-button>
      </div>
    </section>

    <div v-if="notices.length" class="notice" @click="$router.push('/portal/feeds')">
      <el-tag size="small">公告</el-tag>
      <span class="notice-title">{{ notices[0].title }}</span>
      <span class="notice-body">{{ notices[0].content }}</span>
    </div>

    <section class="band person-band">
      <article class="panel person" @click="goMember(home.featuredPerson?.id)">
        <div class="panel-h">本周人物 · {{ home.featuredPerson?.source || '自动选取' }}</div>
        <div v-if="home.featuredPerson" class="person-body">
          <el-image
            v-if="home.featuredPerson.photo || home.featuredPerson.avatar"
            :src="home.featuredPerson.photo || home.featuredPerson.avatar"
            class="person-photo"
            fit="cover"
          >
            <template #error><div class="photo-fallback">{{ home.featuredPerson.name?.[0] }}</div></template>
          </el-image>
          <div v-else class="photo-fallback">{{ home.featuredPerson.name?.[0] }}</div>
          <div class="person-meta">
            <h2>{{ home.featuredPerson.name }}</h2>
            <div class="sub">{{ genLabel(home.featuredPerson) }} · {{ home.featuredPerson.lifeSpan }}</div>
            <p>{{ home.featuredPerson.intro || '生平待补充' }}</p>
          </div>
        </div>
        <div v-else class="quiet">人物持续收录中</div>
      </article>
      <article class="panel">
        <div class="panel-h">
          <span>族影长廊 · {{ home.galleryTotal || 0 }} 张</span>
          <el-button link type="primary" @click="$router.push('/portal/gallery')">全部</el-button>
        </div>
        <div v-if="(home.gallery || []).length" class="gallery">
          <el-image
            v-for="(g, i) in (home.gallery || []).slice(0, 8)"
            :key="g.url + i"
            :src="g.url"
            fit="cover"
            preview-teleported
            :preview-src-list="(home.gallery || []).map((x: any) => x.url)"
            :initial-index="i"
            @click.stop
          >
            <template #error><div class="thumb-fallback" /></template>
          </el-image>
        </div>
        <div v-else class="quiet">照片持续收录中，可在档案中补充</div>
      </article>
    </section>

    <section class="mods">
      <button v-for="m in modules" :key="m.path" class="mod" type="button" @click="$router.push(m.path)">
        <strong>{{ m.title }}</strong>
        <span>{{ m.desc }}</span>
      </button>
    </section>

    <section class="band">
      <article class="panel">
        <div class="panel-h">
          <span>家族日历</span>
          <el-button link type="primary" @click="$router.push('/portal/calendar')">本月</el-button>
        </div>
        <div v-if="calendars.length" class="list">
          <button v-for="c in calendars" :key="c.type + c.title + c.date" class="row" type="button" @click="goLink(c.link)">
            <el-tag size="small">{{ calType(c.type) }}</el-tag>
            <span class="row-main">
              <b>{{ c.title }}</b>
              <i>{{ fmtDate(c.date) }}{{ c.remark ? ' · ' + c.remark : '' }}</i>
            </span>
          </button>
        </div>
        <div v-else class="quiet">近期暂无纪念日</div>
      </article>
      <article class="panel">
        <div class="panel-h">
          <span>事迹精选</span>
          <el-button link type="primary" @click="$router.push('/portal/achievements')">事迹库</el-button>
        </div>
        <div v-if="deeds.length" class="list">
          <button v-for="d in deeds" :key="d.kind + d.id" class="row" type="button" @click="openDeed(d)">
            <el-tag v-if="d.pinned" size="small" type="danger">置顶</el-tag>
            <span class="row-main">
              <b>{{ d.name }} · {{ d.title }}</b>
              <i>{{ d.content }}</i>
            </span>
          </button>
        </div>
        <div v-else class="quiet">事迹持续整理中</div>
      </article>
    </section>

    <section class="band">
      <article class="panel">
        <div class="panel-h">
          <span>家族动态</span>
          <el-button link type="primary" @click="$router.push('/portal/feeds')">更多</el-button>
        </div>
        <div v-if="feedList.length" class="list">
          <div v-for="f in feedList" :key="f.id" class="row feed">
            <div class="row-main">
              <b><el-tag size="small">{{ f.type === 1 ? '公告' : f.type === 3 ? '公示' : '动态' }}</el-tag> {{ f.title }}</b>
              <i>{{ f.content }}</i>
            </div>
            <div v-if="f.images?.length" class="thumbs">
              <el-image v-for="(img, i) in f.images.slice(0, 3)" :key="img + i" :src="img" fit="cover" :preview-src-list="f.images" :initial-index="i" preview-teleported />
            </div>
            <div class="acts">
              <el-button link @click="like(f)">赞 {{ f.likeCount || 0 }}</el-button>
              <el-button link @click="$router.push('/portal/feeds')">评论 {{ f.commentCount || 0 }}</el-button>
            </div>
          </div>
        </div>
        <div v-else class="quiet">暂无动态</div>
      </article>
      <article class="panel">
        <div class="panel-h">
          <span>助学榜样</span>
          <el-button link type="primary" @click="$router.push('/portal/scholarship')">学海无涯</el-button>
        </div>
        <div v-if="wall.length" class="wall">
          <div v-for="w in wall" :key="w.applicationId" class="wall-item">
            <b>{{ w.name }}</b>
            <span>{{ w.year }}</span>
            <i>{{ [w.school, w.major, w.grade].filter(Boolean).join(' · ') }}</i>
          </div>
        </div>
        <div v-else class="quiet">榜样墙将在发放完成后展示</div>
        <div v-if="logged && config" class="cta">
          <div>{{ config.open ? '资助窗口开放中' : '资助窗口未开放' }}</div>
          <div>申请时间：{{ format(config.windowStart) }} 至 {{ format(config.windowEnd) }}</div>
          <el-button type="primary" :disabled="!config.open" @click="$router.push('/portal/scholarship-apply')">立即申请</el-button>
        </div>
      </article>
    </section>
  </div>
</template>
<script setup lang="ts">
import { GenealogyContentApi, GenealogyFeedApi, GenealogyScholarshipApi, GenealogyShowcaseApi } from '@/api/genealogy'
import { isLoggedIn, PORTAL_LOGIN } from '@/utils/portalAuth'
defineOptions({ name: 'PortalHome' })
const router = useRouter()
const logged = computed(() => isLoggedIn())
const family = ref<any>({})
const notices = ref<any[]>([])
const config = ref<any>()
const home = ref<any>({})
const modules = [
  { title: '谱书', desc: '在线翻阅 · 按辈按支', path: '/portal/book' },
  { title: '基础族谱', desc: '谱系树 · 成员档案', path: '/portal/tree' },
  { title: '寻根问祖', desc: '源流 · 迁徙 · 字辈', path: '/portal/roots' },
  { title: '清明祭祖', desc: '活动报名 · 坟地导航', path: '/portal/ancestor' },
  { title: '族影长廊', desc: '既有照片汇聚展示', path: '/portal/gallery' },
  { title: '家族日历', desc: '寿辰 · 忌日 · 活动', path: '/portal/calendar' },
  { title: '迁徙故事', desc: '节点卡片 · 高德路线', path: '/portal/migration' },
  { title: '学海无涯', desc: '助学榜样 · 资助申请', path: '/portal/scholarship' }
]
const calendars = computed(() => (home.value.calendar || []).slice(0, 6))
const deeds = computed(() => (home.value.featuredDeeds || []).slice(0, 5))
const feedList = computed(() => (home.value.feeds || []).slice(0, 4))
const wall = computed(() => (home.value.scholarshipWall || []).slice(0, 6))
const format = (t: number) => (t ? new Date(t).toLocaleDateString() : '')
const fmtDate = (t: any) => {
  if (!t) return ''
  const d = new Date(t)
  return Number.isNaN(d.getTime()) ? String(t).slice(0, 10) : d.toLocaleDateString()
}
const calType = (t: string) => ({ birthday: '寿辰', memorial: '忌日', festival: '节气', activity: '活动', scholarship: '资助' }[t] || t)
const genLabel = (p: any) => [p.generationNo ? p.generationNo + '世' : '', p.generationWord ? p.generationWord + '字辈' : ''].filter(Boolean).join(' · ')
const goMember = (id?: number) => id && router.push('/portal/member?id=' + id)
const goLink = (link?: string) => link && router.push(link)
const openDeed = (d: any) => {
  if (d.kind === 'member' && d.memberId) router.push('/portal/member?id=' + d.memberId)
  else router.push('/portal/achievements')
}
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
onMounted(async () => {
  try { family.value = await GenealogyContentApi.getFamily() } catch {}
  try { home.value = (await GenealogyShowcaseApi.home()) || {} } catch { home.value = {} }
  try { notices.value = (await GenealogyFeedApi.frontPage({ pageNo: 1, pageSize: 5, type: 1, status: 1 })).list || [] } catch {}
  if (logged.value) {
    try { config.value = await GenealogyScholarshipApi.getConfig() } catch {}
  }
})
</script>
<style scoped>
.home { display: flex; flex-direction: column; gap: 18px; }
.hero {
  border-radius: 14px;
  background: linear-gradient(135deg, #3a3126 0%, #55462f 55%, #7a5c36 100%);
  color: #f6f1e6;
  padding: 36px 40px 32px;
}
.hero h1 { font-size: 32px; margin: 14px 0 10px; }
.intro { margin: 0; max-width: 920px; line-height: 1.7; opacity: .9; display: -webkit-box; -webkit-line-clamp: 3; -webkit-box-orient: vertical; overflow: hidden; }
.hero-actions { display: flex; gap: 12px; flex-wrap: wrap; margin-top: 22px; }
.tag { display: inline-block; border: 1px solid rgba(246,241,230,.3); padding: 4px 14px; border-radius: 999px; font-size: 12px; }
.notice {
  display: flex; align-items: center; gap: 10px;
  background: #fffdf7; border: 1px solid #e8dfcc; border-radius: 12px;
  padding: 12px 18px; cursor: pointer; min-height: 48px;
}
.notice-title { font-weight: 700; flex-shrink: 0; }
.notice-body { color: #8b8273; font-size: 13px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.band { display: grid; grid-template-columns: 1fr 1fr; gap: 16px; align-items: stretch; }
.person-band { grid-template-columns: 1.05fr 1.35fr; }
.panel {
  background: #fffdf7;
  border: 1px solid #e8dfcc;
  border-radius: 12px;
  padding: 16px 18px 14px;
  min-height: 240px;
  display: flex;
  flex-direction: column;
}
.panel-h {
  display: flex; align-items: center; justify-content: space-between;
  font-weight: 700; margin-bottom: 12px; color: #2a251f;
}
.person { cursor: pointer; }
.person-body { display: grid; grid-template-columns: 108px 1fr; gap: 16px; min-height: 0; }
.person-photo, .photo-fallback {
  width: 108px; height: 132px; border-radius: 10px; background: #efe7d6; object-fit: cover;
}
.photo-fallback { display: flex; align-items: center; justify-content: center; font-size: 32px; color: #a63d2f; }
.person-meta h2 { margin: 0; font-size: 22px; }
.sub { color: #8b8273; font-size: 13px; margin: 6px 0 8px; }
.person-meta p { margin: 0; font-size: 13px; line-height: 1.7; color: #5c5348; display: -webkit-box; -webkit-line-clamp: 4; -webkit-box-orient: vertical; overflow: hidden; }
.gallery { display: grid; grid-template-columns: repeat(4, minmax(0, 1fr)); gap: 8px; flex: 1; }
.gallery :deep(.el-image) { width: 100%; height: 78px; border-radius: 8px; overflow: hidden; background: #efe7d6; }
.thumb-fallback { width: 100%; height: 100%; background: #efe7d6; }
.quiet { flex: 1; display: flex; align-items: center; justify-content: center; color: #8b8273; font-size: 13px; background: #f8f3e8; border-radius: 8px; min-height: 120px; }
.mods { display: grid; grid-template-columns: repeat(4, minmax(0, 1fr)); gap: 12px; }
.mod {
  text-align: left; background: #fffdf7; border: 1px solid #e8dfcc; border-radius: 12px;
  padding: 16px 18px; cursor: pointer; min-height: 86px;
}
.mod:hover { border-color: #a63d2f; }
.mod strong { display: block; font-size: 16px; }
.mod span { display: block; margin-top: 6px; color: #8b8273; font-size: 12px; }
.list { display: flex; flex-direction: column; gap: 0; }
.row {
  display: flex; align-items: flex-start; gap: 8px;
  width: 100%; text-align: left; background: transparent; border: 0; border-bottom: 1px solid #f0e8d8;
  padding: 10px 0; cursor: pointer; color: inherit;
}
.row:last-child { border-bottom: 0; }
.row-main { display: flex; flex-direction: column; gap: 4px; min-width: 0; flex: 1; }
.row-main b { font-size: 14px; font-weight: 700; }
.row-main i { font-style: normal; color: #8b8273; font-size: 12px; line-height: 1.6; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; }
.feed { flex-direction: column; }
.thumbs { display: flex; gap: 6px; }
.thumbs :deep(.el-image) { width: 56px; height: 56px; border-radius: 6px; overflow: hidden; background: #efe7d6; }
.acts { display: flex; gap: 8px; }
.wall { display: grid; grid-template-columns: 1fr 1fr; gap: 10px; }
.wall-item {
  background: #f8f3e8; border-radius: 8px; padding: 10px 12px; display: grid; gap: 4px;
}
.wall-item b { font-size: 14px; }
.wall-item span { color: #a63d2f; font-size: 12px; }
.wall-item i { font-style: normal; color: #8b8273; font-size: 12px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.cta { margin-top: auto; padding-top: 12px; border-top: 1px dashed #eadfcb; color: #5c5348; font-size: 13px; display: grid; gap: 6px; }
@media (max-width: 1100px) {
  .band, .person-band, .mods { grid-template-columns: 1fr 1fr; }
}
@media (max-width: 720px) {
  .band, .person-band, .mods, .person-body, .wall, .gallery { grid-template-columns: 1fr; }
  .hero { padding: 24px 20px; }
}
</style>
