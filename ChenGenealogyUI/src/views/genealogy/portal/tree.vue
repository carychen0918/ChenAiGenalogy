<template>
  <div>
    <div class="toolbar">
      <div>
        <div class="text-22px font-bold">族谱世系</div>
        <div class="text-13px text-gray-500">按世代自上而下展开，按所属房区分字辈。选择成员后点寻根，沿父系直系逐级高亮，连线同步点亮</div>
      </div>
      <el-space wrap>
        <el-radio-group v-model="mode">
          <el-radio-button label="tree">树形图</el-radio-button>
          <el-radio-button label="list">列表图</el-radio-button>
        </el-radio-group>
        <el-button v-if="meId" type="primary" @click="locateMe">定位我</el-button>
        <el-select
          v-if="meId"
          v-model="relateTo"
          filterable
          remote
          clearable
          placeholder="查与我的关系"
          class="!w-220px"
          :remote-method="searchRelate"
          :loading="relateLoading"
          @change="loadRelation"
        >
          <el-option v-for="m in relateOptions" :key="m.id" :label="m.name" :value="m.id" />
        </el-select>
        <el-input v-model="kw" placeholder="搜索成员姓名 / 几世 / 字辈" class="!w-240px" clearable @keyup.enter="globalSearch" @clear="hits = []" />
      </el-space>
    </div>
    <div class="seek-bar">
      <span class="seek-label">寻根</span>
      <el-select
        v-model="seekId"
        filterable
        clearable
        placeholder="选择成员姓名（同名会显示几世、父亲、生卒）"
        class="!w-360px"
      >
        <el-option v-for="m in seekOptions" :key="m.id" :label="lineagePickLabel(m, memberById)" :value="m.id" />
      </el-select>
      <el-button type="primary" :disabled="!seekId" :loading="seeking" @click="seekRoot()">寻根</el-button>
      <el-button v-if="lineageIds.length" @click="clearLineage">清除高亮</el-button>
    </div>
    <div v-if="lineagePath.length" class="path-bar lineage-bar">
      <span class="text-13px text-gray-500 mr-8px">直系寻根</span>
      <el-space wrap>
        <el-tag
          v-for="(m, i) in lineagePath"
          :key="m.id"
          :type="Number(m.id) === Number(lineageNowId) ? 'danger' : ''"
          class="cursor-pointer"
          @click="chartRef?.locate?.(m.id)"
        >
          {{ i ? '↑ ' : '' }}{{ m.generationNo ? m.generationNo + '世 · ' : '' }}{{ m.name }}
        </el-tag>
      </el-space>
    </div>
    <div v-if="path.length" class="path-bar">
      <span class="text-13px text-gray-500 mr-8px">关系路径</span>
      <el-space wrap>
        <el-tag v-for="(h, i) in path" :key="h.memberId" class="cursor-pointer" @click="$router.push('/portal/member?id=' + h.memberId)">
          {{ i ? h.edge + ' · ' : '' }}{{ h.name }}
        </el-tag>
      </el-space>
      <el-empty v-if="relateTo && !path.length && !relateLoading" description="谱上暂未找到通路" />
    </div>
    <div v-if="kw">
      <el-card v-for="m in filtered" :key="m.id" class="mb-8px cursor-pointer" @click="$router.push('/portal/member?id=' + m.id)">
        {{ m.name }} · {{ formatMemberGeneration(m) }}
      </el-card>
      <el-empty v-if="!filtered.length" :description="'未找到成员“' + kw + '”'" />
    </div>
    <div v-else v-loading="loading">
      <PedigreeChart
        v-if="mode === 'tree'"
        ref="chartRef"
        :members="list"
        :highlight-id="meId"
        :lineage-ids="lineageIds"
        :lineage-now-id="lineageNowId"
        :lineage-self-id="lineageSelfId"
        @select="openCard"
      />
      <div v-else class="list-wrap">
        <div v-for="gen in grouped" :key="gen.no" class="mb-16px">
          <div class="text-12px text-gray-500 mb-8px">{{ gen.no }}世{{ gen.words ? ' · ' + gen.words : '' }}</div>
          <div class="flex flex-wrap gap-12px">
            <div
              v-for="m in gen.nodes"
              :key="m.id"
              class="node"
              :class="{
                me: meId && Number(m.id) === Number(meId),
                lineage: lineageIds.includes(Number(m.id)),
                'lineage-self': Number(m.id) === Number(lineageSelfId)
              }"
              @click="openCard(m)"
            >
              <el-avatar :size="36" :src="m.avatar">{{ m.name?.[0] }}</el-avatar>
              <div class="font-bold mt-6px">{{ m.name }}</div>
              <div class="text-12px text-gray-500">{{ formatGenerationWord(m) ? formatGenerationWord(m) + '字辈 · ' : '' }}{{ formatLifeSpan(m) }}</div>
            </div>
          </div>
        </div>
      <el-empty v-if="!list.length" description="族谱录入中" />
      </div>
    </div>
    <el-dialog v-model="cardVisible" title="成员卡片" width="560px">
      <div v-if="card" class="thick-card">
        <div class="thick-head">
          <el-avatar :size="72" :src="card.avatar || card.photoUrls?.[0]">{{ card.name?.[0] }}</el-avatar>
          <div>
            <div class="text-18px font-bold">{{ card.name }}</div>
            <div class="mt-6px">{{ formatMemberGeneration(card) || '—' }} · {{ card.gender === 1 ? '男' : '女' }}</div>
            <div class="mt-6px text-gray-500">生卒：{{ formatLifeSpan(card) }}</div>
            <div class="mt-6px">配偶：{{ (card.spouseNames || []).join('、') || '—' }}</div>
          </div>
        </div>
        <div v-if="card.intro" class="mt-12px intro">{{ card.intro }}</div>
        <div v-else class="mt-12px text-gray-400 text-13px">暂无简介</div>
        <div v-if="(card.photoUrls || []).length" class="photos">
          <el-image
            v-for="(p, i) in card.photoUrls.slice(0, 6)"
            :key="p + i"
            :src="p"
            class="photo"
            fit="cover"
            preview-teleported
            :preview-src-list="card.photoUrls"
            :initial-index="i"
          />
        </div>
      </div>
      <template #footer>
        <el-button v-if="card?.id" @click="seekFromCard">从此人寻根</el-button>
        <el-button v-if="meId && card?.id && Number(card.id) !== Number(meId)" @click="viewRelation">查看与我的关系</el-button>
        <el-button type="primary" @click="$router.push('/portal/member?id=' + card.id)">查看详情</el-button>
      </template>
    </el-dialog>
  </div>
</template>
<script setup lang="ts">
import { GenealogyMemberApi, GenealogyShowcaseApi } from '@/api/genealogy'
import PedigreeChart from '@/views/genealogy/components/PedigreeChart.vue'
import { formatGenerationWord, formatMemberGeneration } from '@/views/genealogy/utils/generation'
import { formatLifeSpan, isSpouseOnlyMember, lineagePickLabel, paternalLineage } from '@/views/genealogy/utils/member'
import { ElMessage } from 'element-plus'

defineOptions({ name: 'PortalTree' })
const loading = ref(false)
const list = ref<any[]>([])
const kw = ref('')
const mode = ref('tree')
const cardVisible = ref(false)
const card = ref<any>()
const chartRef = ref<any>()
const meId = ref<number>()
const relateTo = ref<number>()
const relateOptions = ref<any[]>([])
const relateLoading = ref(false)
const path = ref<any[]>([])
const hits = ref<any[]>([])
const seekId = ref<number>()
const seeking = ref(false)
const lineageIds = ref<number[]>([])
const lineageNowId = ref<number | null>(null)
const lineageSelfId = ref<number | null>(null)
const lineagePath = ref<any[]>([])
let seekSeq = 0
const wait = (ms: number) => new Promise((resolve) => setTimeout(resolve, ms))

const openCard = async (m: any) => {
  cardVisible.value = true
  card.value = m
  try {
    card.value = await GenealogyMemberApi.get(m.id)
  } catch {
    card.value = m
  }
}
const grouped = computed(() => {
  const map = new Map<number, any>()
  list.value
    .filter((m) => !isSpouseOnlyMember(m) && Number(m.generationNo) > 0)
    .forEach((m) => {
    const no = Number(m.generationNo)
    if (!map.has(no)) map.set(no, { no, words: new Set<string>(), nodes: [] as any[] })
    const row = map.get(no)
    row.nodes.push(m)
    const label = formatGenerationWord(m)
    if (label) row.words.add(label)
  })
  return [...map.values()]
    .sort((a, b) => a.no - b.no)
    .map((row) => ({ no: row.no, words: [...row.words].join('、') + (row.words.size ? '字辈' : ''), nodes: row.nodes }))
})
const allMembers = ref<any[]>([])
const memberById = computed(() => {
  const map = new Map<number, any>()
  list.value.forEach((m) => map.set(Number(m.id), m))
  allMembers.value.forEach((m) => {
    if (!map.has(Number(m.id))) map.set(Number(m.id), m)
  })
  return map
})
const seekOptions = computed(() =>
  [...list.value]
    .filter((m) => m?.id && m.name && !isSpouseOnlyMember(m))
    .sort((a, b) => String(a.name).localeCompare(String(b.name), 'zh') || (a.generationNo || 0) - (b.generationNo || 0))
)
const filtered = computed(() => {
  const q = kw.value.trim()
  if (!q) return []
  if (hits.value.length) return hits.value
  return allMembers.value.filter((m) => m.name?.includes(q))
})
const locateMe = async () => {
  if (!meId.value) return
  await nextTick()
  chartRef.value?.locate?.(meId.value)
}
const globalSearch = async () => {
  const q = kw.value.trim()
  if (!q) {
    hits.value = []
    return
  }
  try {
    hits.value = (await GenealogyShowcaseApi.search(q)) || []
  } catch {
    hits.value = []
  }
}
const searchRelate = async (q: string) => {
  if (!q?.trim()) return
  relateLoading.value = true
  try {
    relateOptions.value = (await GenealogyShowcaseApi.search(q.trim())) || []
  } finally {
    relateLoading.value = false
  }
}
const loadRelation = async (toId?: number) => {
  path.value = []
  if (!meId.value || !toId) return
  relateLoading.value = true
  try {
    path.value = (await GenealogyShowcaseApi.relation(meId.value, toId)) || []
  } finally {
    relateLoading.value = false
  }
}
const viewRelation = async () => {
  if (!card.value?.id) return
  relateTo.value = card.value.id
  await loadRelation(card.value.id)
  cardVisible.value = false
}
const clearLineage = () => {
  seekSeq += 1
  seeking.value = false
  lineageIds.value = []
  lineageNowId.value = null
  lineageSelfId.value = null
  lineagePath.value = []
}
const seekRoot = async (id?: number) => {
  const target = typeof id === 'number' ? id : seekId.value
  if (!target) return
  kw.value = ''
  hits.value = []
  mode.value = 'tree'
  const pool = list.value.length ? list.value : allMembers.value
  const chain = paternalLineage(target, pool)
  if (!chain.length) {
    ElMessage.warning('谱上未找到该成员')
    return
  }
  const seq = ++seekSeq
  seekId.value = Number(target)
  lineageSelfId.value = Number(target)
  lineagePath.value = chain
  lineageIds.value = []
  lineageNowId.value = null
  seeking.value = true
  await nextTick()
  for (const m of chain) {
    if (seq !== seekSeq) return
    lineageIds.value = [...lineageIds.value, Number(m.id)]
    lineageNowId.value = Number(m.id)
    await nextTick()
    chartRef.value?.locate?.(m.id)
    await wait(520)
  }
  if (seq !== seekSeq) return
  lineageNowId.value = null
  seeking.value = false
}
const seekFromCard = async () => {
  if (!card.value?.id) return
  cardVisible.value = false
  await seekRoot(Number(card.value.id))
}
onMounted(async () => {
  loading.value = true
  try {
    const me = await GenealogyMemberApi.me().catch(() => null)
    meId.value = me?.id
    allMembers.value = await GenealogyMemberApi.simpleList()
    list.value = await GenealogyMemberApi.tree({ up: 30, down: 30 })
    await nextTick()
    if (meId.value) chartRef.value?.locate?.(meId.value)
  } finally {
    loading.value = false
  }
})
</script>
<style scoped>
.toolbar { display: flex; justify-content: space-between; gap: 16px; margin-bottom: 12px; align-items: flex-start; flex-wrap: wrap; }
.seek-bar { display: flex; flex-wrap: wrap; align-items: center; gap: 10px; margin-bottom: 12px; }
.seek-label { font-weight: 700; color: #a63d2f; }
.path-bar { background: #fffdf7; border: 1px solid #e8dfcc; border-radius: 10px; padding: 10px 14px; margin-bottom: 12px; }
.lineage-bar { border-color: #e8c4bc; background: #fff8f5; }
.list-wrap { background: #fffdf7; border: 1px solid #e8dfcc; border-radius: 12px; padding: 20px; }
.node { min-width: 108px; padding: 10px 14px; border: 1px solid #ddd2bc; border-radius: 10px; text-align: center; cursor: pointer; background: #fff; display: flex; flex-direction: column; align-items: center; }
.node.me, .node.lineage { border-color: #a63d2f; background: #fff5f2; }
.node.lineage-self { box-shadow: 0 0 0 2px rgba(166, 61, 47, 0.35); }
.node:hover { border-color: #a63d2f; }
.intro { line-height: 1.7; color: #5c5348; white-space: pre-wrap; max-height: 160px; overflow: auto; }
.thick-head { display: flex; gap: 16px; align-items: flex-start; }
.photos { display: flex; flex-wrap: wrap; gap: 8px; margin-top: 12px; }
.photo { width: 72px; height: 72px; border-radius: 6px; }
</style>
