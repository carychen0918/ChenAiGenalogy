<template>
  <div class="hud">
    <div class="scan" />
    <div class="grid-bg" />
    <div class="corner tl" /><div class="corner tr" /><div class="corner bl" /><div
      class="corner br"
    />

    <header class="hero">
      <div>
        <div class="kicker">CLAN CORE // 谱核控制台</div>
        <h1>{{ summary.bookTitle || summary.familyName || '陈氏族谱' }}</h1>
        <p class="meta">
          {{ summary.region || '地区待完善' }}
          <span v-if="summary.ancestorName"> · 始祖{{ summary.ancestorName }}</span>
          <span v-if="summary.bookRevision"> · {{ summary.bookRevision }}</span>
        </p>
      </div>
      <div class="hero-right">
        <div class="clock">{{ clock }}</div>
        <el-button class="portal-btn" @click="router.push('/portal/home')"
          >进入族人端门户</el-button
        >
      </div>
    </header>

    <div class="alerts">
      <button
        v-for="a in alerts"
        :key="a.label"
        class="pill"
        :class="{ hot: a.value > 0 }"
        @click="router.push(a.to)"
      >
        <span class="pill-label">{{ a.label }}</span>
        <span class="pill-num">{{ a.value }}</span>
      </button>
    </div>

    <div class="kpis">
      <article v-for="c in kpis" :key="c.label" class="kpi" @click="c.to && router.push(c.to)">
        <div class="kpi-label">{{ c.en }}</div>
        <div class="kpi-val">{{ c.value }}</div>
        <div class="kpi-name">{{ c.label }}</div>
      </article>
    </div>

    <div class="panels">
      <section class="panel">
        <div class="panel-h">近期任务清单 <span>TASK STREAM</span></div>
        <div v-if="!summary.todos?.length" class="empty">当前没有待处理事项</div>
        <button v-for="(t, i) in summary.todos" :key="i" class="todo" @click="router.push(t.route)">
          <span class="tag">{{ typeLabel(t.type) }}</span>
          <span class="todo-title">{{ t.title }}</span>
          <span class="todo-time">{{ fmt(t.time) }}</span>
        </button>
      </section>
      <section class="panel">
        <div class="panel-h">谱务健康度 <span>LINEAGE HEALTH</span></div>
        <div class="health">
          <div class="h-row" v-for="h in health" :key="h.label">
            <span>{{ h.label }}</span>
            <b>{{ h.value }}</b>
          </div>
        </div>
        <div
          class="ritual"
          v-if="summary.upcomingActivity"
          @click="router.push('/genealogy/worship/activity')"
        >
          <div class="kicker">NEXT RITUAL</div>
          <div class="ritual-title">{{ summary.upcomingActivity.title }}</div>
          <div class="todo-time">
            {{ fmt(summary.upcomingActivity.startTime) }}
            · {{ summary.upcomingActivity.place }} · 已报
            {{ summary.upcomingActivity.joinedCount || 0 }}/{{
              summary.upcomingActivity.maxCount || '-'
            }}
          </div>
        </div>
      </section>
    </div>

    <div class="links">
      <button
        v-for="l in summary.links || []"
        :key="l.route"
        class="hex"
        @click="router.push(l.route)"
      >
        <div class="hex-label">{{ l.label }}</div>
        <div class="hex-hint">{{ l.hint }}</div>
      </button>
    </div>
  </div>
</template>
<script setup lang="ts">
import { GenealogyDashboardApi } from '@/api/genealogy'

defineOptions({ name: 'GenealogyHome' })
const router = useRouter()
const summary = ref<any>({})
const clock = ref('')
let timer: ReturnType<typeof setInterval>
const n = (v: any) => (v == null ? 0 : v)
const alerts = computed(() => [
  { label: '待审档案', value: n(summary.value.pendingArchive), to: '/genealogy/family/archive' },
  { label: '待审动态', value: n(summary.value.pendingFeed), to: '/genealogy/ops/feed' },
  { label: '待审评论', value: n(summary.value.pendingFeedComment), to: '/genealogy/ops/feed' },
  { label: '资助待审', value: n(summary.value.pendingAudit), to: '/genealogy/scholarship/audit' },
  {
    label: '待发放',
    value: n(summary.value.pendingDisburse),
    to: '/genealogy/scholarship/disburse'
  },
  { label: '待补材料', value: n(summary.value.pendingMaterial), to: '/genealogy/scholarship/audit' }
])
const kpis = computed(() => [
  {
    en: 'MEMBERS',
    label: '成员总数',
    value: n(summary.value.memberCount),
    to: '/genealogy/family/member'
  },
  {
    en: 'ALIVE',
    label: '在世',
    value: n(summary.value.aliveCount),
    to: '/genealogy/family/member'
  },
  {
    en: 'INCOMPLETE',
    label: '待完善',
    value: n(summary.value.incompleteCount),
    to: '/genealogy/family/member'
  },
  {
    en: 'QUEUE',
    label: '待办合计',
    value: n(summary.value.pendingTotal),
    to: '/genealogy/family/archive'
  },
  { en: 'BOOK', label: '谱书页数', value: n(summary.value.bookPages), to: '/portal/book' },
  {
    en: 'GEN',
    label: '最大世代',
    value: n(summary.value.maxGeneration),
    to: '/genealogy/family/generation'
  }
])
const health = computed(() => [
  {
    label: '字辈条目 / 在用',
    value: `${n(summary.value.generationCount)} / ${n(summary.value.generationInUse)}`
  },
  { label: '迁徙节点', value: n(summary.value.migrationCount) },
  { label: '先贤事迹', value: n(summary.value.deedCount) },
  { label: '文化指南', value: n(summary.value.cultureCount) },
  { label: '谱序', value: summary.value.bookPrefaceReady ? '已录入' : '待修' },
  {
    label: '资助窗口',
    value: summary.value.scholarshipWindowOpen
      ? `开放 ${summary.value.scholarshipYear || ''}`
      : '未开放'
  },
  { label: '回收站', value: n(summary.value.recycleCount) }
])
const typeLabel = (t: string) =>
  ({ ARCHIVE: '档案', FEED: '动态', SCHOLARSHIP: '资助', COMMENT: '评论' })[t] || t
const fmt = (t: string) => {
  if (!t) return ''
  const d = new Date(t)
  if (Number.isNaN(d.getTime())) return String(t).replace('T', ' ').slice(0, 16)
  const p = (n: number) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${p(d.getMonth() + 1)}-${p(d.getDate())} ${p(d.getHours())}:${p(d.getMinutes())}`
}
const tick = () => {
  const d = new Date()
  const p = (n: number) => String(n).padStart(2, '0')
  clock.value = `${d.getFullYear()}.${p(d.getMonth() + 1)}.${p(d.getDate())} ${p(d.getHours())}:${p(d.getMinutes())}:${p(d.getSeconds())}`
}
onMounted(async () => {
  tick()
  timer = setInterval(tick, 1000)
  try {
    summary.value = (await GenealogyDashboardApi.summary()) || {}
  } catch {
    summary.value = {}
  }
})
onUnmounted(() => timer && clearInterval(timer))
</script>
<style scoped>
.hud {
  --ink: #d8ecec;
  --dim: #7aa0a4;
  --cyan: #4ecdc4;
  --gold: #d4a574;
  --red: #c45c4a;
  --panel: rgba(10, 16, 22, 0.82);
  position: relative;
  min-height: calc(100vh - 128px);
  padding: 28px 28px 36px;
  border-radius: 14px;
  overflow: hidden;
  color: var(--ink);
  background:
    radial-gradient(1200px 480px at 12% -10%, rgba(196, 92, 74, 0.18), transparent 55%),
    radial-gradient(900px 500px at 100% 0%, rgba(78, 205, 196, 0.12), transparent 50%), #07090e;
}
.grid-bg {
  pointer-events: none;
  position: absolute;
  inset: 0;
  background-image:
    linear-gradient(rgba(78, 205, 196, 0.06) 1px, transparent 1px),
    linear-gradient(90deg, rgba(78, 205, 196, 0.06) 1px, transparent 1px);
  background-size: 42px 42px;
  mask-image: radial-gradient(circle at 50% 20%, #000 35%, transparent 80%);
}
.scan {
  pointer-events: none;
  position: absolute;
  inset: 0;
  background: linear-gradient(
    180deg,
    transparent 0%,
    rgba(78, 205, 196, 0.08) 50%,
    transparent 100%
  );
  background-size: 100% 220%;
  animation: scan 7s linear infinite;
}
@keyframes scan {
  0% {
    background-position: 0 -80%;
  }
  100% {
    background-position: 0 180%;
  }
}
.corner {
  position: absolute;
  width: 22px;
  height: 22px;
  border: 2px solid var(--cyan);
  z-index: 2;
}
.tl {
  top: 10px;
  left: 10px;
  border-right: 0;
  border-bottom: 0;
}
.tr {
  top: 10px;
  right: 10px;
  border-left: 0;
  border-bottom: 0;
}
.bl {
  bottom: 10px;
  left: 10px;
  border-right: 0;
  border-top: 0;
}
.br {
  bottom: 10px;
  right: 10px;
  border-left: 0;
  border-top: 0;
}
.hero,
.alerts,
.kpis,
.panels,
.links {
  position: relative;
  z-index: 1;
}
.hero {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  align-items: flex-end;
  margin-bottom: 22px;
}
.kicker {
  letter-spacing: 4px;
  color: var(--cyan);
  font-size: 11px;
  margin-bottom: 8px;
}
h1 {
  margin: 0;
  font-size: 32px;
  letter-spacing: 6px;
  font-weight: 700;
  color: #f4f7f7;
}
.meta {
  margin: 8px 0 0;
  color: var(--dim);
  font-size: 13px;
}
.hero-right {
  text-align: right;
}
.clock {
  font-variant-numeric: tabular-nums;
  color: var(--gold);
  letter-spacing: 2px;
  margin-bottom: 10px;
}
.portal-btn {
  background: transparent !important;
  border: 1px solid var(--cyan) !important;
  color: var(--cyan) !important;
}
.alerts {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-bottom: 22px;
}
.pill {
  display: flex;
  align-items: center;
  gap: 10px;
  border: 1px solid rgba(78, 205, 196, 0.28);
  background: rgba(12, 22, 28, 0.7);
  color: var(--ink);
  border-radius: 999px;
  padding: 8px 14px;
  cursor: pointer;
}
.pill.hot {
  border-color: var(--red);
  box-shadow: 0 0 12px rgba(196, 92, 74, 0.35);
}
.pill-label {
  color: var(--dim);
  font-size: 12px;
}
.pill-num {
  font-size: 18px;
  font-weight: 700;
  color: var(--cyan);
}
.pill.hot .pill-num {
  color: #ffb4a8;
}
.kpis {
  display: grid;
  grid-template-columns: repeat(6, minmax(0, 1fr));
  gap: 12px;
  margin-bottom: 18px;
}
.kpi {
  background: var(--panel);
  border: 1px solid rgba(78, 205, 196, 0.22);
  padding: 16px 14px 14px;
  cursor: pointer;
  clip-path: polygon(
    10px 0,
    100% 0,
    100% calc(100% - 10px),
    calc(100% - 10px) 100%,
    0 100%,
    0 10px
  );
}
.kpi:hover {
  border-color: var(--gold);
}
.kpi-label {
  font-size: 10px;
  letter-spacing: 2px;
  color: var(--cyan);
}
.kpi-val {
  font-size: 30px;
  font-weight: 700;
  margin: 6px 0 2px;
  color: #fff;
}
.kpi-name {
  font-size: 12px;
  color: var(--dim);
}
.panels {
  display: grid;
  grid-template-columns: 1.2fr 0.8fr;
  gap: 14px;
  margin-bottom: 18px;
}
.panel {
  background: var(--panel);
  border: 1px solid rgba(212, 165, 116, 0.18);
  padding: 16px 18px 10px;
  min-height: 280px;
}
.panel-h {
  font-size: 15px;
  font-weight: 700;
  margin-bottom: 12px;
  display: flex;
  justify-content: space-between;
}
.panel-h span {
  color: var(--dim);
  font-size: 11px;
  letter-spacing: 2px;
  font-weight: 400;
}
.todo {
  width: 100%;
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 0;
  border: 0;
  border-bottom: 1px dashed rgba(78, 205, 196, 0.16);
  background: transparent;
  color: inherit;
  cursor: pointer;
  text-align: left;
}
.tag {
  font-size: 11px;
  color: var(--gold);
  border: 1px solid rgba(212, 165, 116, 0.4);
  padding: 1px 6px;
}
.todo-title {
  flex: 1;
}
.todo-time,
.empty {
  color: var(--dim);
  font-size: 12px;
}
.health {
  display: grid;
  gap: 8px;
}
.h-row {
  display: flex;
  justify-content: space-between;
  padding: 6px 0;
  border-bottom: 1px solid rgba(255, 255, 255, 0.04);
  color: var(--dim);
}
.h-row b {
  color: var(--ink);
  font-weight: 600;
}
.ritual {
  margin-top: 16px;
  padding: 12px;
  border: 1px solid rgba(196, 92, 74, 0.45);
  cursor: pointer;
}
.ritual-title {
  font-size: 16px;
  margin: 4px 0;
}
.links {
  display: grid;
  grid-template-columns: repeat(6, minmax(0, 1fr));
  gap: 10px;
}
.hex {
  background: rgba(12, 20, 28, 0.8);
  border: 1px solid rgba(78, 205, 196, 0.3);
  color: var(--ink);
  padding: 16px 8px;
  cursor: pointer;
  clip-path: polygon(18% 0, 82% 0, 100% 50%, 82% 100%, 18% 100%, 0 50%);
}
.hex:hover {
  border-color: var(--gold);
  color: var(--gold);
}
.hex-label {
  font-size: 16px;
  font-weight: 700;
}
.hex-hint {
  font-size: 11px;
  color: var(--dim);
  margin-top: 4px;
}
@media (max-width: 1200px) {
  .kpis,
  .links {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }
  .panels {
    grid-template-columns: 1fr;
  }
}
</style>
