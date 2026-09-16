<template>
  <ContentWrap>
    <div class="flex flex-wrap gap-12px mb-16px items-center">
      <el-select v-model="rootId" filterable clearable placeholder="以某成员为根" class="!w-280px" @change="load">
        <el-option v-for="m in members" :key="m.id" :label="lineagePickLabel(m, memberById)" :value="m.id" />
      </el-select>
      <el-button @click="load">刷新谱系</el-button>
      <el-select
        v-model="seekId"
        filterable
        clearable
        placeholder="选择成员姓名寻根（同名含几世、父亲、生卒）"
        class="!w-360px"
      >
        <el-option v-for="m in seekOptions" :key="m.id" :label="lineagePickLabel(m, memberById)" :value="m.id" />
      </el-select>
      <el-button type="primary" :disabled="!seekId" :loading="seeking" @click="seekRoot()">寻根</el-button>
      <el-button v-if="lineageIds.length" @click="clearLineage">清除高亮</el-button>
    </div>
    <div v-if="lineagePath.length" class="path-bar">
      <span class="label">直系寻根</span>
      <el-tag
        v-for="(m, i) in lineagePath"
        :key="m.id"
        :type="Number(m.id) === Number(lineageNowId) ? 'danger' : ''"
        class="cursor-pointer mr-8px mb-6px"
        @click="chartRef?.locate?.(m.id)"
      >
        {{ i ? '↑ ' : '' }}{{ m.generationNo ? m.generationNo + '世 · ' : '' }}{{ m.name }}
      </el-tag>
    </div>
    <div v-loading="loading">
      <PedigreeChart
        ref="chartRef"
        :members="list"
        :lineage-ids="lineageIds"
        :lineage-now-id="lineageNowId"
        :lineage-self-id="lineageSelfId"
        @select="open"
      />
    </div>
  </ContentWrap>
</template>
<script setup lang="ts">
import { GenealogyMemberApi } from '@/api/genealogy'
import PedigreeChart from '@/views/genealogy/components/PedigreeChart.vue'
import { isSpouseOnlyMember, lineagePickLabel, paternalLineage } from '@/views/genealogy/utils/member'
import { ElMessage } from 'element-plus'

defineOptions({ name: 'GenealogyPedigree' })
const loading = ref(false)
const list = ref<any[]>([])
const members = ref<any[]>([])
const rootId = ref()
const chartRef = ref<any>()
const seekId = ref<number>()
const seeking = ref(false)
const lineageIds = ref<number[]>([])
const lineageNowId = ref<number | null>(null)
const lineageSelfId = ref<number | null>(null)
const lineagePath = ref<any[]>([])
let seekSeq = 0
const wait = (ms: number) => new Promise((resolve) => setTimeout(resolve, ms))

const memberById = computed(() => {
  const map = new Map<number, any>()
  ;[...list.value, ...members.value].forEach((m) => map.set(Number(m.id), m))
  return map
})
const seekOptions = computed(() =>
  [...(list.value.length ? list.value : members.value)]
    .filter((m) => m?.id && m.name && !isSpouseOnlyMember(m))
    .sort((a, b) => String(a.name).localeCompare(String(b.name), 'zh') || (a.generationNo || 0) - (b.generationNo || 0))
)

const load = async () => {
  loading.value = true
  try {
    list.value = await GenealogyMemberApi.tree({ rootId: rootId.value, up: 30, down: 30 })
  } finally {
    loading.value = false
  }
}
const open = (m: any) => {
  window.open('/portal/member?id=' + m.id, '_blank')
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
  const pool = list.value.length ? list.value : members.value
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
onMounted(async () => {
  members.value = await GenealogyMemberApi.simpleList()
  await load()
})
</script>
<style scoped>
.path-bar {
  background: #fff8f5;
  border: 1px solid #e8c4bc;
  border-radius: 10px;
  padding: 10px 14px;
  margin-bottom: 12px;
}
.path-bar .label {
  color: #8b8273;
  font-size: 13px;
  margin-right: 8px;
}
</style>
