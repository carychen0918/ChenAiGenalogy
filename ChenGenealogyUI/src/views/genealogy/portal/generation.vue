<template>
  <div>
    <div class="text-22px font-bold mb-16px">字辈 · 派语</div>
    <el-card header="派语对照表">
      <el-table :data="rows" border stripe empty-text="字辈录入中" class="poem-table">
        <el-table-column
          v-for="col in POEM_COLUMNS"
          :key="col.key"
          :label="col.label"
          :prop="col.key"
          align="center"
          min-width="110"
        >
          <template #default="s">
            <span :class="{ empty: poemCell(s.row[col.key]) === '—' }">{{ poemCell(s.row[col.key]) }}</span>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
    <el-card class="mt-16px" header="新生儿取名推荐">
      <el-select v-model="fatherGen" placeholder="选择父亲世代" class="!w-240px" @change="rec">
        <el-option v-for="g in rows" :key="g.generationNo" :label="g.chituOrder || g.generationNo + '世'" :value="g.generationNo" />
      </el-select>
      <div class="mt-12px" v-if="word">
        推荐下一辈字辈：{{ word }}
        <div v-if="word !== '未定'" class="mt-8px text-#8b8273">示例：{{ examples }}</div>
      </div>
    </el-card>
  </div>
</template>
<script setup lang="ts">
import { GenealogyContentApi } from '@/api/genealogy'
import { POEM_COLUMNS, poemCell } from '@/views/genealogy/utils/generation'
defineOptions({ name: 'PortalGeneration' })
const rows = ref<any[]>([])
const fatherGen = ref()
const word = ref('')
const nameChar = (raw: string) => raw.replace(/（[^）]*）/g, '').replace(/\([^)]*\)/g, '')
const examples = computed(() => {
  if (!word.value || word.value === '未定') return ''
  return word.value
    .split('、')
    .map((w) => {
      const ch = nameChar(w)
      return `陈${ch}华、陈${ch}宇`
    })
    .join('；')
})
const rec = async () => {
  word.value = await GenealogyContentApi.recommend(fatherGen.value)
}
onMounted(async () => {
  rows.value = (await GenealogyContentApi.poemTable()) || []
})
</script>
<style scoped>
.poem-table :deep(.el-table__header th) {
  background: #f7f1e6;
  color: #5c5144;
  font-weight: 700;
}
.empty {
  color: #c4b8a8;
}
</style>
