<template>
  <ContentWrap>
    <div class="flex gap-12px mb-16px">
      <el-select v-model="rootId" filterable clearable placeholder="以某成员为根" class="!w-240px" @change="load">
        <el-option v-for="m in members" :key="m.id" :label="m.name" :value="m.id" />
      </el-select>
      <el-button @click="load">刷新谱系</el-button>
    </div>
    <div v-loading="loading">
      <PedigreeChart :members="list" @select="open" />
    </div>
  </ContentWrap>
</template>
<script setup lang="ts">
import { GenealogyMemberApi } from '@/api/genealogy'
import PedigreeChart from '@/views/genealogy/components/PedigreeChart.vue'

defineOptions({ name: 'GenealogyPedigree' })
const loading = ref(false)
const list = ref<any[]>([])
const members = ref<any[]>([])
const rootId = ref()

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
onMounted(async () => {
  members.value = await GenealogyMemberApi.simpleList()
  await load()
})
</script>
