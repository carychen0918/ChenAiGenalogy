<template>
  <div>
    <div class="text-22px font-bold mb-16px">祖先事迹库</div>
    <el-space class="mb-16px">
      <el-button v-for="c in cats" :key="c" :type="cat === c ? 'primary' : 'default'" @click="cat = c; load()">{{ c }}</el-button>
    </el-space>
    <el-row :gutter="16">
      <el-col :span="12" v-for="a in list" :key="a.id" class="mb-16px">
        <el-card class="cursor-pointer" @click="open(a)">
          <el-tag>{{ a.category }}</el-tag> <el-tag type="info">{{ a.source }}</el-tag>
          <h3 class="mt-8px">{{ a.name }} · {{ a.title }}</h3>
          <p class="text-gray-500">{{ (a.content || '').slice(0, 60) }}…</p>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>
<script setup lang="ts">
import { GenealogyContentApi } from '@/api/genealogy'
defineOptions({ name: 'PortalAchievements' })
const cats = ['全部', '义士', '乡贤', '医者', '商贾', '文官', '武将', '学者']
const cat = ref('全部')
const list = ref<any[]>([])
const load = async () => {
  const data = await GenealogyContentApi.deedPage({ pageNo: 1, pageSize: 50, category: cat.value === '全部' ? undefined : cat.value })
  list.value = data.list
}
const open = async (a: any) => {
  const d = await GenealogyContentApi.getDeed(a.id)
  ElMessageBox.alert(d.content, `${d.name} · ${d.title}`)
}
onMounted(load)
</script>
