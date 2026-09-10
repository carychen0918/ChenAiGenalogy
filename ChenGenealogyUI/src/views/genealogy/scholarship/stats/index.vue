<template>
  <ContentWrap>
    <el-form :inline="true">
      <el-form-item label="学年"><el-input-number v-model="year" :controls="false" /></el-form-item>
      <el-form-item><el-button type="primary" @click="load">查询</el-button></el-form-item>
    </el-form>
    <el-row :gutter="16">
      <el-col :span="6"><el-card><div class="text-24px">{{ stats.applyCount }}</div><div>申请人数</div></el-card></el-col>
      <el-col :span="6"><el-card><div class="text-24px">{{ stats.passedCount }}</div><div>通过人数</div></el-card></el-col>
      <el-col :span="6"><el-card><div class="text-24px">{{ stats.disburseAmount }}</div><div>发放总金额</div></el-card></el-col>
    </el-row>
    <el-card class="mt-16px">
      <template #header>公示名单</template>
      <el-button type="primary" @click="publish">脱敏发布到动态</el-button>
      <el-table :data="passed" class="mt-12px">
        <el-table-column label="姓名" prop="memberName">
          <template #default="scope">{{ mask(scope.row.memberName) }}</template>
        </el-table-column>
        <el-table-column label="学校" prop="school" />
        <el-table-column label="类型" prop="type">
          <template #default="scope"><dict-tag :type="DICT_TYPE.GENEALOGY_SCHOLARSHIP_TYPE" :value="scope.row.type" /></template>
        </el-table-column>
      </el-table>
    </el-card>
  </ContentWrap>
</template>
<script setup lang="ts">
import { DICT_TYPE } from '@/utils/dict'
import { GenealogyFeedApi, GenealogyScholarshipApi } from '@/api/genealogy'
defineOptions({ name: 'GenealogyScholarshipStats' })
const message = useMessage()
const year = ref(new Date().getFullYear())
const stats = ref<any>({})
const passed = ref<any[]>([])
const mask = (n: string) => (n ? n[0] + '*'.repeat(Math.max(1, n.length - 1)) : '')
const load = async () => {
  stats.value = await GenealogyScholarshipApi.stats(year.value)
  const data = await GenealogyScholarshipApi.page({ pageNo: 1, pageSize: 100, year: year.value, status: 5 })
  passed.value = data.list
}
const publish = async () => {
  const names = passed.value.map((i) => mask(i.memberName)).join('、')
  await GenealogyFeedApi.create({ type: 3, title: year.value + '年度助学资助名单公示', content: `经审核，本年度共 ${passed.value.length} 位族人获得资助：${names}。公示期 7 天。`, pinned: true })
  message.success('已发布公示')
}
onMounted(load)
</script>
