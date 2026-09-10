<template>
  <div class="pedigree" v-if="layout.nodes.length">
    <div class="axis" :style="{ height: layout.height + 'px' }">
      <div
        v-for="row in layout.rows"
        :key="row.no"
        class="axis-row"
        :style="{ top: row.y + 'px', height: ROW + 'px' }"
      >
        <div class="axis-no">{{ cnGen(row.no) }}</div>
        <div v-if="row.words.length" class="axis-words" :title="row.words.join('、')">
          {{ row.words.join('、') }}
        </div>
      </div>
    </div>
    <div class="canvas" :style="{ width: layout.width + 'px', height: layout.height + 'px' }">
      <svg class="wires" :width="layout.width" :height="layout.height">
        <path v-for="(d, i) in layout.paths" :key="i" :d="d" />
      </svg>
      <button
        v-for="n in layout.nodes"
        :key="n.id"
        class="person"
        :class="{ female: n.gender === 2 }"
        :style="{ left: n.left + 'px', top: n.top + 'px' }"
        type="button"
        @click="emit('select', n.member)"
      >
        <span class="name">{{ n.name }}</span>
        <span v-if="n.spouseText" class="spouse">{{ n.spouseText }}</span>
        <span v-if="n.word" class="word">{{ n.word }}</span>
        <span v-if="n.note" class="note">{{ n.note }}</span>
      </button>
    </div>
  </div>
  <el-empty v-else description="族谱录入中" />
</template>

<script setup lang="ts">
defineOptions({ name: 'PedigreeChart' })

const props = defineProps<{ members: any[] }>()
const emit = defineEmits<{ select: [member: any] }>()

const UNIT = 92
const ROW = 168
const PAD_X = 28
const PAD_Y = 24
const NODE_H = 118
const BAR_GAP = 22

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

const isSpouseOnly = (m: any) =>
  !m.fatherId && m.gender === 2 && Array.isArray(m.spouseIds) && m.spouseIds.length > 0

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
    return { nodes: [], paths: [], rows: [], width: 0, height: 0 }
  }
  const byId = new Map<number, any>(members.map((m) => [m.id, m]))
  const spouseOnly = new Set(members.filter(isSpouseOnly).map((m) => m.id))

  const childrenOf = (id: number) =>
    members
      .filter((m) => m.fatherId === id && !spouseOnly.has(m.id))
      .sort((a, b) => {
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
    .sort((a, b) => (a.generationNo || 0) - (b.generationNo || 0) || a.id - b.id)
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

  const gens = members.map((m) => m.generationNo || 1)
  const minGen = Math.min(...gens)
  const maxGen = Math.max(...gens)
  const yOf = (no: number) => PAD_Y + ((no || minGen) - minGen) * ROW

  const flat: TreeNode[] = []
  const walk = (n: TreeNode) => {
    flat.push(n)
    n.children.forEach(walk)
  }
  roots.forEach(walk)

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
    return {
      id: n.id,
      member: m,
      name: m.name,
      gender: m.gender,
      word: m.generationWord || '',
      spouseText: spouseText(m),
      note: shortNote(m),
      x: n.x,
      y: yOf(m.generationNo),
      left: n.x - 28,
      top: yOf(m.generationNo)
    }
  })

  const paths: string[] = []
  flat.forEach((n) => {
    if (!n.children.length) return
    const parent = nodes.find((x) => x.id === n.id)
    if (!parent) return
    const kids = n.children
      .map((c) => nodes.find((x) => x.id === c.id))
      .filter(Boolean) as { x: number; y: number }[]
    if (!kids.length) return
    const y1 = parent.y + NODE_H
    const yBar = Math.min(...kids.map((k) => k.y)) - BAR_GAP
    const x1 = parent.x
    paths.push(`M ${x1} ${y1} V ${yBar}`)
    if (kids.length === 1) {
      paths.push(`M ${x1} ${yBar} H ${kids[0].x} V ${kids[0].y}`)
    } else {
      const minX = Math.min(...kids.map((k) => k.x))
      const maxX = Math.max(...kids.map((k) => k.x))
      paths.push(`M ${minX} ${yBar} H ${maxX}`)
      kids.forEach((k) => paths.push(`M ${k.x} ${yBar} V ${k.y}`))
    }
  })

  const rowMap = new Map<number, Set<string>>()
  members.forEach((m) => {
    const no = m.generationNo || 0
    if (!rowMap.has(no)) rowMap.set(no, new Set())
    if (m.generationWord) rowMap.get(no)!.add(m.generationWord)
  })
  const rows = [...rowMap.keys()]
    .sort((a, b) => a - b)
    .map((no) => ({ no, words: [...(rowMap.get(no) || [])], y: yOf(no) }))

  const maxX = nodes.reduce((m, n) => Math.max(m, n.x), 0)
  return {
    nodes,
    paths,
    rows,
    width: Math.max(maxX + PAD_X + 60, forestLeft),
    height: yOf(maxGen) + ROW
  }
})
</script>

<style scoped>
.pedigree {
  display: flex;
  align-items: flex-start;
  min-height: 280px;
  max-height: calc(100vh - 180px);
  background: #fbf8f0;
  border: 1px solid #e8dfcc;
  border-radius: 12px;
  overflow: auto;
}
.axis {
  position: sticky;
  left: 0;
  z-index: 2;
  flex: 0 0 64px;
  border-right: 1px solid #eadfcb;
  background: #f4eee2;
}
.axis-row {
  position: absolute;
  left: 0;
  width: 64px;
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
  color: #a63d2f;
  padding-top: 18px;
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
.person:hover .name {
  color: #a63d2f;
}
.person.female .name {
  font-weight: 500;
}
</style>
