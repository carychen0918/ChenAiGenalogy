<template>
  <div>
    <div class="toolbar">
      <div>
        <div class="text-22px font-bold">族谱世系</div>
        <div class="text-13px text-gray-500">按世代自上而下展开，同一世可并列多种字辈。点击姓名查看详情</div>
      </div>
      <el-space>
        <el-radio-group v-model="mode">
          <el-radio-button label="tree">树形图</el-radio-button>
          <el-radio-button label="list">列表图</el-radio-button>
        </el-radio-group>
        <el-input v-model="kw" placeholder="搜索成员姓名" class="!w-240px" clearable />
      </el-space>
    </div>
    <div v-if="kw">
      <el-card v-for="m in filtered" :key="m.id" class="mb-8px cursor-pointer" @click="$router.push('/portal/member?id=' + m.id)">
        {{ m.name }} · {{ m.generationNo }}世{{ m.generationWord ? ' · ' + m.generationWord + '字辈' : '' }}
      </el-card>
      <el-empty v-if="!filtered.length" :description="'未找到成员“' + kw + '”'" />
    </div>
    <div v-else v-loading="loading">
      <PedigreeChart v-if="mode === 'tree'" :members="list" @select="openCard" />
      <div v-else class="list-wrap">
        <div v-for="gen in grouped" :key="gen.no" class="mb-16px">
          <div class="text-12px text-gray-500 mb-8px">{{ gen.no }}世{{ gen.words ? ' · ' + gen.words : '' }}</div>
          <div class="flex flex-wrap gap-12px">
            <div v-for="m in gen.nodes" :key="m.id" class="node" @click="openCard(m)">
              <div class="font-bold">{{ m.name }}</div>
              <div class="text-12px text-gray-500">{{ m.generationWord ? m.generationWord + '字辈 · ' : '' }}{{ life(m) }}</div>
            </div>
          </div>
        </div>
      <el-empty v-if="!list.length" description="族谱录入中" />
      </div>
    </div>
    <el-dialog v-model="cardVisible" title="成员卡片" width="420px">
      <div v-if="card">
        <div class="text-18px font-bold">{{ card.name }}</div>
        <div class="mt-8px">{{ card.generationNo }}世{{ card.generationWord ? ' · ' + card.generationWord + '字辈' : '' }} · {{ card.gender === 1 ? '男' : '女' }}</div>
        <div class="mt-8px text-gray-500">{{ life(card) }}</div>
        <div class="mt-8px">配偶：{{ (card.spouseNames || []).join('、') || '—' }}</div>
      </div>
      <template #footer>
        <el-button type="primary" @click="$router.push('/portal/member?id=' + card.id)">查看详情</el-button>
      </template>
    </el-dialog>
  </div>
</template>
<script setup lang="ts">
import { GenealogyMemberApi } from '@/api/genealogy'
import PedigreeChart from '@/views/genealogy/components/PedigreeChart.vue'

defineOptions({ name: 'PortalTree' })
const loading = ref(false)
const list = ref<any[]>([])
const kw = ref('')
const mode = ref('tree')
const cardVisible = ref(false)
const card = ref<any>()

const openCard = (m: any) => {
  card.value = m
  cardVisible.value = true
}
const grouped = computed(() => {
  const map = new Map<number, any>()
  list.value.forEach((m) => {
    const no = m.generationNo || 0
    if (!map.has(no)) map.set(no, { no, words: new Set<string>(), nodes: [] as any[] })
    const row = map.get(no)
    row.nodes.push(m)
    if (m.generationWord) row.words.add(m.generationWord)
  })
  return [...map.values()]
    .sort((a, b) => a.no - b.no)
    .map((row) => ({ no: row.no, words: [...row.words].join('、') + (row.words.size ? '字辈' : ''), nodes: row.nodes }))
})
const allMembers = ref<any[]>([])
const filtered = computed(() => allMembers.value.filter((m) => m.name?.includes(kw.value.trim())))
const life = (m: any) => {
  const b = m.birthDate ? new Date(m.birthDate).getFullYear() : ''
  const d = m.deathDate ? new Date(m.deathDate).getFullYear() : ''
  return d ? `${b}-${d}` : String(b)
}
onMounted(async () => {
  loading.value = true
  try {
    allMembers.value = await GenealogyMemberApi.simpleList()
    list.value = await GenealogyMemberApi.tree({ up: 30, down: 30 })
  } finally {
    loading.value = false
  }
})
</script>
<style scoped>
.toolbar { display: flex; justify-content: space-between; gap: 16px; margin-bottom: 16px; align-items: flex-start; flex-wrap: wrap; }
.list-wrap { background: #fffdf7; border: 1px solid #e8dfcc; border-radius: 12px; padding: 20px; }
.node { min-width: 108px; padding: 10px 14px; border: 1px solid #ddd2bc; border-radius: 10px; text-align: center; cursor: pointer; background: #fff; }
.node:hover { border-color: #a63d2f; }
</style>
