<template>
  <div>
    <div class="text-22px font-bold mb-16px">字辈 · 派语</div>
    <el-card header="字辈排行表">
      <el-table :data="groups" border>
        <el-table-column label="世代" width="90">
          <template #default="s">{{ s.row.generationNo }}世</template>
        </el-table-column>
        <el-table-column label="字辈">
          <template #default="s">
            <el-tag v-for="item in s.row.items" :key="item.id" class="mr-8px mb-4px">
              {{ item.word }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="220">
          <template #default="s">
            <span v-for="item in s.row.items" :key="item.id" class="mr-8px">
              {{ item.word }}
              <dict-tag :type="DICT_TYPE.GENEALOGY_GENERATION_STATUS" :value="item.status" />
            </span>
          </template>
        </el-table-column>
        <el-table-column label="说明">
          <template #default="s">
            {{ s.row.items.map((i: any) => i.remark).filter(Boolean).join('；') }}
          </template>
        </el-table-column>
      </el-table>
    </el-card>
    <el-card class="mt-16px" header="新生儿取名推荐">
      <el-select v-model="fatherGen" placeholder="选择父亲世代" class="!w-240px" @change="rec">
        <el-option v-for="g in groups" :key="g.generationNo" :label="g.generationNo + '世'" :value="g.generationNo" />
      </el-select>
      <div class="mt-12px" v-if="word">
        推荐下一辈字辈：{{ word }}
        <div v-if="word !== '未定'" class="mt-8px text-#8b8273">示例：{{ examples }}</div>
      </div>
    </el-card>
  </div>
</template>
<script setup lang="ts">
import { DICT_TYPE } from '@/utils/dict'
import { GenealogyContentApi } from '@/api/genealogy'
defineOptions({ name: 'PortalGeneration' })
const list = ref<any[]>([])
const fatherGen = ref()
const word = ref('')
const groups = computed(() => {
  const map = new Map<number, any[]>()
  for (const g of list.value) {
    if (!map.has(g.generationNo)) map.set(g.generationNo, [])
    map.get(g.generationNo)!.push(g)
  }
  return [...map.entries()]
    .sort((a, b) => a[0] - b[0])
    .map(([generationNo, items]) => ({ generationNo, items }))
})
const examples = computed(() => {
  if (!word.value || word.value === '未定') return ''
  return word.value
    .split('、')
    .map((w) => `陈${w}华、陈${w}宇`)
    .join('；')
})
const rec = async () => {
  word.value = await GenealogyContentApi.recommend(fatherGen.value)
}
onMounted(async () => {
  list.value = await GenealogyContentApi.generationList()
})
</script>
