<template>
  <div class="book-page">
    <div class="toolbar">
      <div class="title">{{ meta.bookTitle || '谱书' }}</div>
      <div class="actions">
        <el-select
          v-model="genJump"
          clearable
          filterable
          placeholder="定位字辈 / 世代"
          class="!w-200px"
          @change="jumpGen"
        >
          <el-option
            v-for="g in meta.generations || []"
            :key="g.generationNo"
            :label="`${g.generationNo}世${g.words ? ' · ' + g.words : ''}`"
            :value="g.pageNo"
          />
        </el-select>
        <el-input
          v-model="kw"
          class="!w-220px"
          placeholder="定位人名、字辈、N世"
          clearable
          @keyup.enter="doSearch"
        >
          <template #append>
            <el-button @click="doSearch">定位</el-button>
          </template>
        </el-input>
        <el-button @click="tocOpen = true">目录</el-button>
      </div>
    </div>

    <div v-if="hits.length" class="hits">
      <el-tag
        v-for="(h, i) in hits"
        :key="(h.memberId || h.title) + '-' + i"
        class="mr-8px mb-8px cursor-pointer"
        :type="h.memberId === highlightId ? 'danger' : 'info'"
        @click="jumpTo(h.pageNo, h.memberId)"
      >
        {{ h.memberId ? `${h.generationNo || '?'}世 ${h.name}` : h.title }}
      </el-tag>
    </div>

    <div class="book-frame" @keydown="onKey" tabindex="0" ref="stageRef">
      <div class="spine" />
      <div class="book-viewport">
        <div
          v-for="sheet in sheets"
          :key="sheet.role"
          class="sheet"
          :class="[sheet.role, flipDir && sheet.role === 'leaving' ? 'turn-' + flipDir : '']"
        >
          <div v-if="sheet.data.loginRequired" class="gate">
            <div class="paper-title">世系正文需登录后翻阅</div>
            <p>封面、前言与字辈可公开阅读。登录后可按辈查看全部分支。</p>
            <el-button type="primary" @click="goLogin">去登录</el-button>
          </div>
          <template v-else-if="sheet.data.type === 'COVER'">
            <div class="cover">
              <div class="cover-en">GENEALOGY</div>
              <div class="cover-title">{{ sheet.data.bookTitle }}</div>
              <div class="cover-line" />
              <div class="cover-meta">{{ sheet.data.surname }}氏 · 始祖{{ sheet.data.ancestorName || '待考' }}</div>
              <div class="cover-meta">{{ sheet.data.region }}</div>
              <div class="cover-rev">{{ sheet.data.bookRevision }}</div>
            </div>
          </template>
          <template v-else-if="sheet.data.type === 'PREFACE'">
            <div class="chapter">前言</div>
            <div class="html" v-html="sheet.data.html" />
          </template>
          <template v-else-if="sheet.data.type === 'GENERATION'">
            <div class="chapter">字辈派语</div>
            <el-table :data="sheet.data.generationRows" border size="small" class="poem-table">
              <el-table-column label="赤土官庄世序" min-width="120" align="center">
                <template #default="s">{{ s.row.chituOrder || (s.row.generationNo ? s.row.generationNo + '世' : '—') }}</template>
              </el-table-column>
              <el-table-column label="全国统一字派" prop="nationalSource" min-width="110" align="center">
                <template #default="s">{{ s.row.nationalSource || '—' }}</template>
              </el-table-column>
              <el-table-column label="高安椒坊字派" prop="jiaofangSource" min-width="110" align="center">
                <template #default="s">{{ s.row.jiaofangSource || '—' }}</template>
              </el-table-column>
              <el-table-column label="长房" prop="house1" min-width="80" align="center">
                <template #default="s">{{ s.row.house1 || '—' }}</template>
              </el-table-column>
              <el-table-column label="二房" prop="house2" min-width="80" align="center">
                <template #default="s">{{ s.row.house2 || '—' }}</template>
              </el-table-column>
              <el-table-column label="三房" prop="house3" min-width="80" align="center">
                <template #default="s">{{ s.row.house3 || '—' }}</template>
              </el-table-column>
              <el-table-column label="三房织金" prop="house3Zhijin" min-width="90" align="center">
                <template #default="s">{{ s.row.house3Zhijin || '—' }}</template>
              </el-table-column>
              <el-table-column label="四五房" prop="house45" min-width="80" align="center">
                <template #default="s">{{ s.row.house45 || '—' }}</template>
              </el-table-column>
            </el-table>
          </template>
          <template v-else>
            <div class="chapter">
              {{ sheet.data.title }}
              <span v-if="sheet.data.generationWords" class="words">{{ sheet.data.generationWords }}字辈</span>
            </div>
            <div
              v-for="(b, bi) in sheet.data.branches || []"
              :key="(b.generationWord || '') + '-' + (b.fatherId || 'r') + '-' + bi"
              class="branch"
            >
              <div class="branch-title">{{ b.title }}</div>
              <div
                v-for="m in b.members"
                :key="m.id"
                class="person"
                :class="{ on: m.id === highlightId }"
                @click="openMember(m.id)"
              >
                <div class="person-name">
                  {{ m.name }}
                  <span class="muted">{{ m.gender === 2 ? '女' : '男' }}</span>
                  <span v-if="m.generationWord" class="muted">{{ m.generationWord }}字辈</span>
                  <span v-if="m.lifeSpan" class="muted">{{ m.lifeSpan }}</span>
                </div>
                <div v-if="m.spouseNames" class="muted">配 {{ m.spouseNames }}</div>
                <div v-if="m.intro" class="intro">{{ m.intro }}</div>
                <el-carousel
                  v-if="m.photoUrls?.length"
                  class="album"
                  height="180px"
                  indicator-position="outside"
                  :interval="4000"
                  @click.stop
                >
                  <el-carousel-item v-for="(p, pi) in m.photoUrls" :key="pi">
                    <el-image :src="p" fit="contain" class="album-img" :preview-src-list="m.photoUrls" :initial-index="pi" preview-teleported />
                  </el-carousel-item>
                </el-carousel>
                <div v-if="m.deeds?.length" class="deeds" @click.stop>
                  <div class="deed-h">事迹荣誉</div>
                  <div v-for="d in m.deeds" :key="d.id" class="deed">
                    <div class="deed-title">
                      {{ d.title }}
                      <span v-if="d.occurYear" class="muted">{{ d.occurYear }}</span>
                    </div>
                    <div v-if="d.source" class="muted">来源：{{ d.source }}</div>
                    <div v-if="d.content" class="intro">{{ d.content }}</div>
                  </div>
                </div>
              </div>
            </div>
            <el-empty v-if="!sheet.data.branches?.length" description="本世暂无世系记录" />
          </template>
          <div class="folio">第 {{ sheet.data.pageNo || pageNo }} 页</div>
        </div>
      </div>
    </div>

    <div class="pager-bar">
      <el-button :disabled="pageNo <= 1 || flipping" @click="flip(-1)">上一页</el-button>
      <span class="pager-text">第 {{ pageNo }} 页 / 共 {{ meta.totalPages || 1 }} 页</span>
      <el-button type="primary" :disabled="pageNo >= (meta.totalPages || 1) || flipping" @click="flip(1)">下一页</el-button>
    </div>

    <el-drawer v-model="tocOpen" title="目录" size="320px">
      <div
        v-for="t in meta.toc || []"
        :key="t.pageNo + '-' + t.title"
        class="toc-item"
        :class="{ on: t.pageNo === pageNo }"
        @click="jumpTo(t.pageNo); tocOpen = false"
      >
        <span>{{ t.title }}</span>
        <span class="muted">{{ t.pageNo }}</span>
      </div>
    </el-drawer>
  </div>
</template>
<script setup lang="ts">
import { GenealogyBookApi } from '@/api/genealogy'
import { isLoggedIn } from '@/utils/portalAuth'

defineOptions({ name: 'PortalBook' })
const route = useRoute()
const router = useRouter()
const message = useMessage()
const meta = ref<any>({})
const page = ref<any>({})
const leaving = ref<any>(null)
const pageNo = ref(1)
const kw = ref('')
const hits = ref<any[]>([])
const highlightId = ref<number>()
const tocOpen = ref(false)
const genJump = ref()
const flipping = ref(false)
const flipDir = ref<'next' | 'prev' | ''>('')
const stageRef = ref<HTMLElement>()

const sheets = computed(() => {
  const list: { role: string; data: any }[] = [{ role: 'current', data: page.value || {} }]
  if (flipping.value && leaving.value) {
    list.unshift({ role: 'leaving', data: leaving.value })
  }
  return list
})

const loadPage = async (n: number, memberId?: number) => {
  const total = meta.value.totalPages || 1
  pageNo.value = Math.min(Math.max(1, n), total)
  page.value = (await GenealogyBookApi.page(pageNo.value)) || {}
  highlightId.value = memberId
  nextTick(() => stageRef.value?.focus())
}

const flip = async (dir: number) => {
  const nextNo = pageNo.value + dir
  const total = meta.value.totalPages || 1
  if (flipping.value || nextNo < 1 || nextNo > total) return
  leaving.value = { ...page.value, branches: [...(page.value.branches || [])] }
  flipDir.value = dir > 0 ? 'next' : 'prev'
  flipping.value = true
  await loadPage(nextNo)
  window.setTimeout(() => {
    flipping.value = false
    leaving.value = null
    flipDir.value = ''
  }, 720)
}

const jumpTo = async (n: number, memberId?: number) => {
  if (n === pageNo.value) {
    highlightId.value = memberId
    return
  }
  const dir = n > pageNo.value ? 1 : -1
  leaving.value = { ...page.value, branches: [...(page.value.branches || [])] }
  flipDir.value = dir > 0 ? 'next' : 'prev'
  flipping.value = true
  await loadPage(n, memberId)
  window.setTimeout(() => {
    flipping.value = false
    leaving.value = null
    flipDir.value = ''
  }, 720)
}

const jumpGen = (n: number) => {
  if (n) jumpTo(n)
}

const doSearch = async () => {
  const word = kw.value.trim()
  if (!word) {
    hits.value = []
    return
  }
  const resp = await GenealogyBookApi.search(word)
  if (resp?.loginRequired) {
    message.warning('定位世系人物需先登录')
    return
  }
  hits.value = resp?.list || []
  if (!hits.value.length) {
    message.info('未找到匹配的人或字辈')
    return
  }
  const first = hits.value[0]
  await jumpTo(first.pageNo, first.memberId)
}

const openMember = (id: number) => {
  if (!isLoggedIn()) {
    router.push('/portal/login?redirect=' + encodeURIComponent('/portal/member?id=' + id))
    return
  }
  router.push('/portal/member?id=' + id)
}

const goLogin = () => {
  router.push('/portal/login?redirect=' + encodeURIComponent('/portal/book?page=' + pageNo.value))
}

const onKey = (e: KeyboardEvent) => {
  if (e.key === 'ArrowLeft') flip(-1)
  if (e.key === 'ArrowRight') flip(1)
}

onMounted(async () => {
  meta.value = (await GenealogyBookApi.meta()) || {}
  const q = Number(route.query.page || 1)
  const mid = route.query.memberId ? Number(route.query.memberId) : undefined
  await loadPage(q, mid)
})
</script>
<style scoped>
.book-page { max-width: 920px; margin: 0 auto; }
.toolbar { display: flex; justify-content: space-between; align-items: center; gap: 12px; margin-bottom: 16px; flex-wrap: wrap; }
.title { font-size: 22px; font-weight: 700; }
.actions { display: flex; gap: 8px; flex-wrap: wrap; }
.hits { margin-bottom: 12px; }
.book-frame {
  display: flex;
  outline: none;
  min-height: 640px;
}
.spine {
  width: 14px;
  flex-shrink: 0;
  background: #8d3a30;
  border-radius: 4px 0 0 4px;
}
.book-viewport {
  flex: 1;
  perspective: 1800px;
  position: relative;
  min-height: 640px;
  overflow: hidden;
}
.sheet {
  background: #fbf6e8;
  border: 1px solid #e0d3b4;
  border-left: none;
  padding: 36px 40px 48px;
  color: #3a3126;
  min-height: 640px;
  box-sizing: border-box;
  transform-origin: left center;
  backface-visibility: hidden;
}
.sheet.current { position: relative; z-index: 1; }
.sheet.leaving {
  position: absolute;
  inset: 0;
  z-index: 3;
  background-image: linear-gradient(90deg, #efe4c8 0%, #fbf6e8 18px, #fbf6e8 100%);
}
.sheet.turn-next {
  transform-origin: left center;
  animation: bookFlipNext .7s ease forwards;
}
.sheet.turn-prev {
  transform-origin: right center;
  animation: bookFlipPrev .7s ease forwards;
}
@keyframes bookFlipNext {
  0% { transform: rotateY(0); }
  70% { transform: rotateY(-140deg); }
  100% { transform: rotateY(-180deg); }
}
@keyframes bookFlipPrev {
  0% { transform: rotateY(0); }
  70% { transform: rotateY(140deg); }
  100% { transform: rotateY(180deg); }
}
.cover { text-align: center; padding: 80px 20px 40px; }
.cover-en { letter-spacing: 8px; color: #a63d2f; font-size: 12px; }
.cover-title { font-size: 40px; font-weight: 700; margin: 24px 0 16px; letter-spacing: 8px; }
.cover-line { width: 80px; height: 2px; background: #a63d2f; margin: 0 auto 24px; }
.cover-meta { color: #6b6254; margin-top: 8px; }
.cover-rev { margin-top: 48px; color: #a63d2f; }
.chapter { font-size: 22px; font-weight: 700; margin-bottom: 20px; border-left: 3px solid #a63d2f; padding-left: 10px; }
.words { font-size: 14px; font-weight: 400; color: #8b8273; margin-left: 10px; }
.html :deep(p) { line-height: 2; text-indent: 2em; margin-bottom: 12px; }
.branch { margin-bottom: 22px; }
.branch-title { color: #a63d2f; font-weight: 700; margin-bottom: 8px; }
.person { padding: 10px 0; border-bottom: 1px dashed #e8dfcc; cursor: pointer; }
.person.on { background: #f4e3de; margin: 0 -12px; padding: 10px 12px; }
.person-name { font-weight: 700; }
.muted { color: #8b8273; font-weight: 400; font-size: 13px; margin-left: 8px; }
.intro { margin-top: 6px; font-size: 13px; line-height: 1.7; color: #5c5348; }
.album { margin-top: 10px; background: #f3ead4; }
.album-img { width: 100%; height: 180px; }
.deeds { margin-top: 10px; padding-top: 8px; border-top: 1px dashed #e8dfcc; }
.deed-h { color: #a63d2f; font-weight: 700; font-size: 13px; margin-bottom: 6px; }
.deed { margin-bottom: 8px; }
.deed-title { font-weight: 600; }
.folio { position: absolute; right: 24px; bottom: 16px; color: #8b8273; font-size: 13px; }
.sheet { position: relative; }
.pager-bar {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 24px;
  margin-top: 16px;
}
.pager-text { color: #8b8273; min-width: 140px; text-align: center; }
.gate { text-align: center; padding: 120px 20px; }
.paper-title { font-size: 20px; font-weight: 700; margin-bottom: 12px; }
.toc-item { display: flex; justify-content: space-between; padding: 10px 0; border-bottom: 1px solid #f0e6d4; cursor: pointer; }
.toc-item.on { color: #a63d2f; font-weight: 700; }
</style>
