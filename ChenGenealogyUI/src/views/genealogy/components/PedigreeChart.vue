<template>
  <div class="pedigree-wrap" v-if="layout.nodes.length">
    <div class="legend">
      <div v-if="layout.houses.length" class="legend-houses">
        <span class="legend-label">所属房</span>
        <span v-for="h in layout.houses" :key="h.value" class="legend-item" :class="'house-' + h.value">
          <i />{{ h.label }}
        </span>
      </div>
      <div class="zoom-bar">
        <span class="zoom-tip">滚轮缩放 · 按住拖动</span>
        <button type="button" class="zoom-btn" @click="zoomBy(1 / 1.12)">－</button>
        <span class="zoom-pct">{{ Math.round(scale * 100) }}%</span>
        <button type="button" class="zoom-btn" @click="zoomBy(1.12)">＋</button>
        <button type="button" class="zoom-btn" @click="resetZoom">还原</button>
      </div>
    </div>
    <div
      ref="viewportRef"
      class="pedigree"
      :class="{ dragging: dragging }"
      @wheel.prevent="onWheel"
      @pointerdown="onPointerDown"
      @pointermove="onPointerMove"
      @pointerup="onPointerUp"
      @pointercancel="onPointerCancel"
    >
      <div
        class="scale-box"
        :style="{
          width: scaledWidth + 'px',
          height: scaledHeight + 'px'
        }"
      >
        <div
          class="scale-inner"
          :style="{
            width: innerWidth + 'px',
            height: layout.height + 'px',
            transform: `scale(${scale})`
          }"
        >
          <div class="axis" :style="{ height: layout.height + 'px' }">
            <div
              v-for="row in layout.rows"
              :key="row.no"
              class="axis-row"
              :style="{ top: row.y + 'px', height: ROW + 'px' }"
            >
              <div class="axis-no">{{ cnGen(row.no) }}</div>
              <div v-if="row.words.length" class="axis-words" :title="row.words.join('、')">
                <span v-for="(w, i) in row.words" :key="w + i" :class="row.houseClasses[i]">{{ w }}</span>
              </div>
            </div>
          </div>
          <div class="canvas" :style="{ width: layout.width + 'px', height: layout.height + 'px' }">
            <svg class="wires" :width="layout.width" :height="layout.height">
              <path v-for="(w, i) in layout.paths" :key="'n' + i" :d="w.d" />
              <path v-for="(w, i) in lineageWires" :key="'h' + i" class="lineage" :d="w.d" />
            </svg>
            <button
              v-for="n in layout.nodes"
              :key="n.id"
              class="person"
              :class="[
                'house-' + (n.house || '0'),
                {
                  female: n.gender === 2,
                  me: isHighlight(n.id),
                  lineage: isLineagePerson(n.id),
                  'lineage-self': isLineageSelf(n.id),
                  'lineage-now': isLineageNow(n.id)
                }
              ]"
              :style="{ left: n.left + 'px', top: n.top + 'px' }"
              type="button"
            >
              <img v-if="n.avatar" class="avatar" :src="n.avatar" alt="" />
              <span v-else class="avatar-fallback">{{ n.name?.[0] }}</span>
              <span class="name">{{ n.name }}</span>
              <span v-if="n.spouseText" class="spouse">{{ n.spouseText }}</span>
              <span v-if="n.word" class="word">{{ n.word }}</span>
              <span v-if="n.houseLabel" class="house">{{ n.houseLabel }}</span>
              <span v-if="n.source && n.source !== n.word" class="source">源{{ n.source }}</span>
              <span v-if="n.note" class="note">{{ n.note }}</span>
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
  <el-empty v-else description="族谱录入中" />
</template>

<script setup lang="ts">
import { HOUSE_LABEL, houseLabel } from '@/views/genealogy/utils/generation'
import { isSpouseOnlyMember } from '@/views/genealogy/utils/member'

defineOptions({ name: 'PedigreeChart' })

type Wire = { d: string; parentId: number; childIds: number[] }

const props = defineProps<{
  members: any[]
  highlightId?: number | string | null
  lineageIds?: (number | string)[]
  lineageNowId?: number | string | null
  lineageSelfId?: number | string | null
}>()
const emit = defineEmits<{ select: [member: any] }>()

const UNIT = 108
const ROW = 176
const PAD_X = 28
const PAD_Y = 24
const NODE_H = 126
const BAR_GAP = 22
const AXIS_W = 88
const MIN_SCALE = 0.4
const MAX_SCALE = 1

const scale = ref(1)
const viewportRef = ref<HTMLElement>()
const dragging = ref(false)
const moved = ref(false)
let dragStart = { x: 0, y: 0, left: 0, top: 0 }
let activePointer = -1
const DRAG_THRESHOLD = 6

const clampScale = (value: number) => Math.min(MAX_SCALE, Math.max(MIN_SCALE, value))

const applyZoom = (next: number, origin?: { x: number; y: number }) => {
  const el = viewportRef.value
  const current = scale.value
  const target = clampScale(next)
  if (Math.abs(target - current) < 0.001) return
  if (el && origin) {
    const mx = origin.x + el.scrollLeft
    const my = origin.y + el.scrollTop
    const ratio = target / current
    scale.value = target
    nextTick(() => {
      el.scrollLeft = mx * ratio - origin.x
      el.scrollTop = my * ratio - origin.y
    })
    return
  }
  scale.value = target
}

const onWheel = (e: WheelEvent) => {
  const factor = Math.exp(-e.deltaY * 0.0018)
  const el = viewportRef.value
  const origin = el
    ? { x: e.clientX - el.getBoundingClientRect().left, y: e.clientY - el.getBoundingClientRect().top }
    : undefined
  applyZoom(scale.value * factor, origin)
}

const zoomBy = (factor: number) => {
  const el = viewportRef.value
  const origin = el ? { x: el.clientWidth / 2, y: el.clientHeight / 2 } : undefined
  applyZoom(scale.value * factor, origin)
}

const resetZoom = () => {
  scale.value = 1
}

const eventToInner = (e: PointerEvent) => {
  const el = viewportRef.value
  if (!el) return null
  const rect = el.getBoundingClientRect()
  const zoom = scale.value || 1
  return {
    x: (e.clientX - rect.left + el.scrollLeft) / zoom,
    y: (e.clientY - rect.top + el.scrollTop) / zoom
  }
}

const pickMember = (innerX: number, innerY: number) => {
  const canvasX = innerX - AXIS_W
  if (canvasX < -8) return null
  let best: any = null
  let bestDist = Infinity
  for (const n of layout.value.nodes) {
    const w = Math.max(UNIT - 8, 72)
    if (canvasX < n.left - 8 || canvasX > n.left + w) continue
    if (innerY < n.top - 6 || innerY > n.top + NODE_H + 6) continue
    const dist = Math.hypot(canvasX - (n.left + 18), innerY - (n.top + 36))
    if (dist < bestDist) {
      bestDist = dist
      best = n.member
    }
  }
  return best
}

const releasePointer = (el: HTMLElement | undefined, pointerId: number) => {
  if (!el) return
  try {
    if (el.hasPointerCapture(pointerId)) el.releasePointerCapture(pointerId)
  } catch {
    /* already released */
  }
}

const onPointerDown = (e: PointerEvent) => {
  if (e.button !== 0) return
  const el = viewportRef.value
  if (!el) return
  activePointer = e.pointerId
  dragging.value = true
  moved.value = false
  dragStart = { x: e.clientX, y: e.clientY, left: el.scrollLeft, top: el.scrollTop }
}

const onPointerMove = (e: PointerEvent) => {
  if (!dragging.value || e.pointerId !== activePointer) return
  const el = viewportRef.value
  if (!el) return
  const dx = e.clientX - dragStart.x
  const dy = e.clientY - dragStart.y
  if (!moved.value && Math.abs(dx) + Math.abs(dy) <= DRAG_THRESHOLD) return
  if (!moved.value) {
    moved.value = true
    try {
      el.setPointerCapture(e.pointerId)
    } catch {
      /* ignore */
    }
  }
  el.scrollLeft = dragStart.left - dx
  el.scrollTop = dragStart.top - dy
}

const onPointerUp = (e: PointerEvent) => {
  if (activePointer !== -1 && e.pointerId !== activePointer) return
  const el = viewportRef.value
  releasePointer(el, e.pointerId)
  const wasDrag = moved.value
  dragging.value = false
  moved.value = false
  activePointer = -1
  if (wasDrag) return
  const pt = eventToInner(e)
  if (!pt) return
  const member = pickMember(pt.x, pt.y)
  if (member) emit('select', member)
}

const onPointerCancel = (e: PointerEvent) => {
  if (activePointer !== -1 && e.pointerId !== activePointer) return
  releasePointer(viewportRef.value, e.pointerId)
  dragging.value = false
  moved.value = false
  activePointer = -1
}

watch(
  () => props.members,
  () => {
    scale.value = 1
  }
)
const cnGen = (no: number) => {
  const map = ['零', '一', '二', '三', '四', '五', '六', '七', '八', '九', '十']
  if (no <= 10) return `${map[no] || no}世`
  return `${no}世`
}

const year = (v?: string) => (v ? new Date(v).getFullYear() : '')

const shortNote = (m: any) => {
  const intro = String(m.intro || '').replace(/\s+/g, '')
  if (!intro) return ''
  if (intro.includes('失考')) return '失考'
  return intro.slice(0, 10)
}

const isSpouseOnly = (m: any) => isSpouseOnlyMember(m)

const houseRank = (house?: string) => {
  const n = Number(house)
  return Number.isFinite(n) && n > 0 ? n : 99
}

type TreeNode = {
  id: number
  member: any
  children: TreeNode[]
  width: number
  x: number
}

const layout = computed(() => {
  const members = (props.members || []).filter((m) => m && m.id)
  if (!members.length) {
    return { nodes: [], paths: [], rows: [], houses: [], width: 0, height: 0 }
  }
  const byId = new Map<number, any>(members.map((m) => [m.id, m]))
  const spouseOnly = new Set(members.filter(isSpouseOnly).map((m) => m.id))

  const childrenOf = (id: number) =>
    members
      .filter((m) => m.fatherId === id && !spouseOnly.has(m.id))
      .sort((a, b) => {
        const ah = houseRank(a.generationHouse)
        const bh = houseRank(b.generationHouse)
        if (ah !== bh) return ah - bh
        const ay = year(a.birthDate) || 0
        const by = year(b.birthDate) || 0
        return ay - by || a.id - b.id
      })

  const build = (m: any): TreeNode => ({
    id: m.id,
    member: m,
    children: childrenOf(m.id).map(build),
    width: UNIT,
    x: 0
  })

  const roots = members
    .filter((m) => !m.fatherId && !spouseOnly.has(m.id))
    .sort((a, b) => {
      const g = (a.generationNo || 0) - (b.generationNo || 0)
      if (g) return g
      const h = houseRank(a.generationHouse) - houseRank(b.generationHouse)
      return h || a.id - b.id
    })
    .map(build)

  const measure = (n: TreeNode): number => {
    n.width = n.children.length ? n.children.reduce((sum, c) => sum + measure(c), 0) : UNIT
    n.width = Math.max(UNIT, n.width)
    return n.width
  }
  roots.forEach(measure)

  const place = (n: TreeNode, left: number) => {
    if (!n.children.length) {
      n.x = left + n.width / 2
      return
    }
    let cursor = left
    n.children.forEach((c) => {
      place(c, cursor)
      cursor += c.width
    })
    n.x = (n.children[0].x + n.children[n.children.length - 1].x) / 2
  }

  let forestLeft = 0
  roots.forEach((r) => {
    place(r, forestLeft)
    forestLeft += r.width + UNIT / 2
  })

  const flat: TreeNode[] = []
  const walk = (n: TreeNode) => {
    flat.push(n)
    n.children.forEach(walk)
  }
  roots.forEach(walk)

  const occupiedNos = [
    ...new Set(
      flat
        .map((n) => Number(n.member.generationNo))
        .filter((no) => Number.isFinite(no) && no > 0)
    )
  ].sort((a, b) => a - b)
  const rowIndex = new Map(occupiedNos.map((no, i) => [no, i]))
  const yOf = (no?: number) => {
    const key = Number(no) > 0 ? Number(no) : occupiedNos[0]
    const idx = key ? (rowIndex.get(key) ?? 0) : 0
    return PAD_Y + idx * ROW
  }

  const spouseText = (m: any) => {
    const names = (m.spouseNames || []).filter(Boolean)
    if (names.length) {
      return names.map((n: string) => `配${n.replace(/^陈/, '')}`).join('')
    }
    const ids: number[] = m.spouseIds || []
    const fetched = ids.map((id) => byId.get(id)?.name).filter(Boolean)
    return fetched.map((n: string) => `配${n.replace(/^陈/, '')}`).join('')
  }

  const nodes = flat.map((n) => {
    const m = n.member
    const house = m.generationHouse || ''
    return {
      id: n.id,
      member: m,
      name: m.name,
      avatar: m.avatar || (m.photoUrls && m.photoUrls[0]) || '',
      gender: m.gender,
      word: m.generationWord || '',
      house,
      houseLabel: houseLabel(house),
      source: m.generationNationalSource || '',
      spouseText: spouseText(m),
      note: shortNote(m),
      x: n.x,
      y: yOf(m.generationNo),
      left: n.x - 34,
      top: yOf(m.generationNo)
    }
  })

  const paths: Wire[] = []
  flat.forEach((n) => {
    if (!n.children.length) return
    const parent = nodes.find((x) => x.id === n.id)
    if (!parent) return
    const kids = n.children
      .map((c) => nodes.find((x) => x.id === c.id))
      .filter(Boolean) as { id: number; x: number; y: number }[]
    if (!kids.length) return
    const y1 = parent.y + NODE_H
    const yBar = Math.min(...kids.map((k) => k.y)) - BAR_GAP
    const x1 = parent.x
    const childIds = kids.map((k) => k.id)
    paths.push({ d: `M ${x1} ${y1} V ${yBar}`, parentId: n.id, childIds })
    kids.forEach((k) => {
      paths.push({ d: `M ${x1} ${yBar} H ${k.x}`, parentId: n.id, childIds: [k.id] })
      paths.push({ d: `M ${k.x} ${yBar} V ${k.y}`, parentId: n.id, childIds: [k.id] })
    })
  })

  const rowMap = new Map<number, { labels: string[]; classes: string[] }>()
  occupiedNos.forEach((no) => rowMap.set(no, { labels: [], classes: [] }))
  flat.forEach((n) => {
    const no = Number(n.member.generationNo)
    if (!rowMap.has(no)) return
    const word = n.member.generationWord
    if (!word) return
    const house = n.member.generationHouse || ''
    const label = houseLabel(house) ? `${word}·${houseLabel(house)}` : word
    const row = rowMap.get(no)!
    if (!row.labels.includes(label)) {
      row.labels.push(label)
      row.classes.push(house ? 'house-' + house : '')
    }
  })
  const rows = occupiedNos.map((no) => {
    const row = rowMap.get(no)!
    return { no, words: row.labels, houseClasses: row.classes, y: yOf(no) }
  })

  const houseSet = new Set<string>()
  members.forEach((m) => {
    if (m.generationHouse && HOUSE_LABEL[m.generationHouse]) houseSet.add(m.generationHouse)
  })
  const houses = [...houseSet]
    .sort((a, b) => houseRank(a) - houseRank(b))
    .map((value) => ({ value, label: HOUSE_LABEL[value] }))

  const maxX = nodes.reduce((m, n) => Math.max(m, n.x), 0)
  const lastNo = occupiedNos[occupiedNos.length - 1]
  return {
    nodes,
    paths,
    rows,
    houses,
    width: Math.max(maxX + PAD_X + 60, forestLeft),
    height: (lastNo ? yOf(lastNo) : PAD_Y) + ROW
  }
})

const innerWidth = computed(() => AXIS_W + layout.value.width)
const scaledWidth = computed(() => innerWidth.value * scale.value)
const scaledHeight = computed(() => layout.value.height * scale.value)

const lineageSet = computed(() => new Set((props.lineageIds || []).map((id) => Number(id))))
const isHighlight = (id: number) => props.highlightId != null && Number(props.highlightId) === Number(id)
const isLineagePerson = (id: number) => lineageSet.value.has(Number(id))
const isLineageSelf = (id: number) => props.lineageSelfId != null && Number(props.lineageSelfId) === Number(id)
const isLineageNow = (id: number) => props.lineageNowId != null && Number(props.lineageNowId) === Number(id)
const isLineageWire = (w: Wire) =>
  isLineagePerson(w.parentId) && w.childIds.some((id) => isLineagePerson(id))
const lineageWires = computed(() => layout.value.paths.filter(isLineageWire))

const locate = (id?: number | string | null) => {
  const targetId = id != null ? Number(id) : Number(props.highlightId)
  if (!targetId) return false
  const n = layout.value.nodes.find((x) => Number(x.id) === targetId)
  const el = viewportRef.value
  if (!n || !el) return false
  const left = (AXIS_W + n.left) * scale.value - el.clientWidth / 2 + 40
  const top = n.top * scale.value - el.clientHeight / 2 + 40
  el.scrollTo({ left: Math.max(0, left), top: Math.max(0, top), behavior: 'smooth' })
  return true
}

defineExpose({ locate })
</script>

<style scoped>
.pedigree-wrap {
  display: flex;
  flex-direction: column;
  min-height: 280px;
  max-height: calc(100vh - 180px);
  background: #fbf8f0;
  border: 1px solid #e8dfcc;
  border-radius: 12px;
  overflow: hidden;
}
.legend {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 10px 14px 8px;
  border-bottom: 1px solid #eadfcb;
  background: #f7f1e6;
  color: #6d6254;
  font-size: 12px;
}
.legend-houses {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 12px;
}
.zoom-bar {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  margin-left: auto;
}
.zoom-tip {
  color: #8b8273;
}
.zoom-pct {
  min-width: 44px;
  text-align: center;
  font-variant-numeric: tabular-nums;
  font-weight: 700;
}
.zoom-btn {
  min-width: 28px;
  height: 26px;
  padding: 0 8px;
  border: 1px solid #ddd2bc;
  border-radius: 6px;
  background: #fffdf7;
  color: #5c5348;
  cursor: pointer;
}
.zoom-btn:hover {
  border-color: #a63d2f;
  color: #a63d2f;
}
.legend-label {
  font-weight: 700;
}
.legend-item {
  display: inline-flex;
  align-items: center;
  gap: 6px;
}
.legend-item i {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: currentColor;
}
.pedigree {
  display: flex;
  align-items: flex-start;
  flex: 1;
  overflow: auto;
  cursor: grab;
  user-select: none;
  touch-action: none;
}
.pedigree.dragging {
  cursor: grabbing;
}
.scale-box {
  flex: 0 0 auto;
  position: relative;
  overflow: hidden;
}
.scale-inner {
  display: flex;
  align-items: flex-start;
  transform-origin: 0 0;
}
.axis {
  position: sticky;
  left: 0;
  z-index: 2;
  flex: 0 0 88px;
  border-right: 1px solid #eadfcb;
  background: #f4eee2;
}
.axis-row {
  position: absolute;
  left: 0;
  width: 88px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: flex-start;
  padding-top: 8px;
  color: #6d6254;
}
.axis-no {
  writing-mode: vertical-rl;
  letter-spacing: 4px;
  font-weight: 700;
  font-size: 15px;
}
.axis-words {
  margin-top: 8px;
  writing-mode: vertical-rl;
  letter-spacing: 2px;
  font-size: 12px;
  color: #a63d2f;
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.canvas {
  position: relative;
  flex: 0 0 auto;
}
.wires {
  position: absolute;
  inset: 0;
  pointer-events: none;
}
.wires path {
  fill: none;
  stroke: #8d8273;
  stroke-width: 1.2;
}
.wires path.lineage {
  stroke: #a63d2f;
  stroke-width: 3.2;
  stroke-linecap: round;
  stroke-linejoin: round;
  filter: drop-shadow(0 0 3px rgba(166, 61, 47, 0.45));
}
.person {
  position: absolute;
  display: flex;
  gap: 4px;
  align-items: flex-start;
  padding: 0;
  border: 0;
  background: transparent;
  cursor: pointer;
  color: #2a251f;
  font-family: 'Noto Serif SC', 'STSong', 'SimSun', serif;
}
.person .name {
  writing-mode: vertical-rl;
  letter-spacing: 3px;
  font-size: 18px;
  font-weight: 700;
  line-height: 1.15;
}
.person .spouse {
  writing-mode: vertical-rl;
  letter-spacing: 2px;
  font-size: 13px;
  color: #7a7164;
  padding-top: 14px;
}
.person .word {
  writing-mode: vertical-rl;
  letter-spacing: 2px;
  font-size: 12px;
  color: currentColor;
  padding-top: 18px;
}
.person .house {
  writing-mode: vertical-rl;
  letter-spacing: 2px;
  font-size: 12px;
  font-weight: 700;
  color: currentColor;
  padding-top: 18px;
}
.person .source {
  writing-mode: vertical-rl;
  letter-spacing: 1px;
  font-size: 11px;
  color: #8b8273;
  padding-top: 16px;
}
.person .note {
  writing-mode: vertical-rl;
  letter-spacing: 1px;
  font-size: 11px;
  color: #8b8273;
  max-height: 110px;
  overflow: hidden;
  padding-top: 10px;
}
.person .avatar,
.person .avatar-fallback {
  width: 20px;
  height: 24px;
  object-fit: cover;
  border-radius: 2px;
  flex-shrink: 0;
  background: #f4ece0;
  color: #a63d2f;
  font-size: 11px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-top: 2px;
  border: 1px solid #e8dfcc;
}
.person.me,
.person.lineage {
  box-shadow: 0 0 0 3px rgba(166, 61, 47, 0.35);
  background: #fff5f2;
  border-radius: 4px;
  padding: 2px 4px 2px 2px;
}
.person.lineage .name {
  color: #a63d2f;
}
.person.lineage-self .name {
  font-weight: 800;
}
.person.lineage-now {
  animation: lineage-pulse 0.55s ease;
}
@keyframes lineage-pulse {
  0% {
    transform: scale(1);
  }
  40% {
    transform: scale(1.08);
  }
  100% {
    transform: scale(1);
  }
}
.person:hover .name {
  color: #a63d2f;
}
.person.female .name {
  font-weight: 500;
}
.house-1,
.person.house-1 .word,
.person.house-1 .house {
  color: #a63d2f;
}
.house-2,
.person.house-2 .word,
.person.house-2 .house {
  color: #2f5fa6;
}
.house-3,
.person.house-3 .word,
.person.house-3 .house {
  color: #6b4aa3;
}
.house-4,
.person.house-4 .word,
.person.house-4 .house {
  color: #3a7a45;
}
.house-5,
.person.house-5 .word,
.person.house-5 .house {
  color: #8a5a22;
}
.person.house-0 .word {
  color: #a63d2f;
}
</style>
